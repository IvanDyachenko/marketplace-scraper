package net.dalytics

import cats.tagless.syntax.functorK._
import cats.{Applicative, Monad}
import cats.effect.{Concurrent, Resource}
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.WithRun
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{commitBatchWithin, CommittableOffset, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}

import net.dalytics.config.Config
import net.dalytics.context.MessageContext
import net.dalytics.services.Parse
import net.dalytics.models.{Command, Event}
import net.dalytics.models.parser.{ParserCommand, ParserEvent}
import cats.effect.Temporal

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Temporal: Concurrent,
    F[_]: Applicative: WithRun[*[_], I, MessageContext]
  ](config: Config)(
    parse: Parse[F],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], ParserCommand.ParseOzonResponse]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.kafkaConsumerConfig.maxConcurrentPerTopic) { committable =>
            val offset  = committable.offset
            val command = committable.record.value
            val context = MessageContext(
              consumerGroupId = offset.consumerGroupId,
              topic = offset.topicPartition.topic,
              partition = offset.topicPartition.partition,
              offset = offset.offsetAndMetadata.offset,
              metadata = offset.offsetAndMetadata.metadata
            )

            runContext(parse.handle(command))(context).map { eventsE =>
              eventsE.toOption.fold[(List[ParserEvent], CommittableOffset[I])](List.empty -> offset)(_ -> offset)
            }
          }
          .map { case (events, offset) =>
            val records = events.map {
              case event: ParserEvent.OzonSellerListItemParsed              =>
                ProducerRecord(config.kafkaProducerConfig.topic("results-ozon-seller-list-items"), event.key, event)
              case event: ParserEvent.OzonCategorySearchResultsV2ItemParsed =>
                ProducerRecord(config.kafkaProducerConfig.topic("results-ozon-category-search-results-v2-items"), event.key, event)
            }

            ProducerRecords(records, offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(config.kafkaProducerConfig.maxBufferSize)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.kafkaConsumerConfig.commitEveryNOffsets, config.kafkaConsumerConfig.commitTimeWindow))
      }.parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Temporal,
    F[_]: Applicative: WithRun[*[_], I, MessageContext],
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    parse: Parse[F],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], ParserCommand.ParseOzonResponse]
  ): Resource[I, Parser[S]] =
    Resource.eval {
      Stream
        .eval {
          val impl: Parser[Stream[I, *]] = new Impl[I, F](config)(parse, producerOfEvents, consumerOfCommands)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
