package net.dalytics

import cats.tagless.syntax.functorK._
import cats.{Applicative, Monad}
import cats.effect.{Concurrent, Resource, Timer}
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
import net.dalytics.models.parser.{ParserCommand => Command, ParserEvent => Event}

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: Applicative: WithRun[*[_], I, MessageContext]
  ](config: Config)(
    parse: Parse[F],
    consumer: KafkaConsumer[I, Unit, Command.ParseOzonResponse],
    producer: KafkaProducer[I, Event.Key, Event]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumer.partitionedStream.map { partition =>
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

            runContext(parse.handle(command))(context).map(
              _.toOption.fold[(List[Event], CommittableOffset[I])](List.empty -> offset)(_ -> offset)
            )
          }
          .map { case (events, offset) =>
            val records = events.map { event =>
              val topic = event match {
                case _: Event.OzonSellerListItemParsed               =>
                  config.kafkaProducerConfig.topic("results-ozon-seller-list-items")
                case _: Event.OzonCategorySearchResultsV2ItemParsed  =>
                  config.kafkaProducerConfig.topic("results-ozon-category-search-results-v2-items")
                case _: Event.OzonCategorySoldOutResultsV2ItemParsed =>
                  config.kafkaProducerConfig.topic("results-ozon-category-sold-out-results-v2-items")
              }

              ProducerRecord(topic, event.key, event)
            }

            ProducerRecords(records, offset)
          }
          .evalMap(producer.produce)
          .parEvalMap(config.kafkaProducerConfig.parallelism)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.kafkaConsumerConfig.commitEveryNOffsets, config.kafkaConsumerConfig.commitTimeWindow))
      }.parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: Applicative: WithRun[*[_], I, MessageContext],
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    parse: Parse[F],
    consumer: KafkaConsumer[I, Unit, Command.ParseOzonResponse],
    producer: KafkaProducer[I, Event.Key, Event]
  ): Resource[I, Parser[S]] =
    Resource.eval {
      Stream
        .eval {
          val impl: Parser[Stream[I, *]] = new Impl[I, F](config)(parse, consumer, producer)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
