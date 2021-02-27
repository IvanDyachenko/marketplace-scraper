package marketplace.modules

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

import marketplace.config.ParserConfig
import marketplace.context.AppContext
import marketplace.services.Parse
import marketplace.models.{Command, Event}
import marketplace.models.parser.{ParserCommand, ParserEvent}

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: Applicative: WithRun[*[_], I, AppContext]
  ](config: ParserConfig)(
    parse: Parse[F],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], ParserCommand.ParseOzonResponse]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.kafkaConsumer.maxConcurrentPerPartition) { committable =>
            runContext(parse.handle(committable.record.value))(AppContext()).map(
              _.toOption.fold[(List[ParserEvent], CommittableOffset[I])](List.empty -> committable.offset)(_ -> committable.offset)
            )
          }
          .collect { case (events, offset) =>
            ProducerRecords(events.map(event => ProducerRecord(config.kafkaProducer.topic, event.key, event)), offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(config.kafkaProducer.maxBufferSize)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.kafkaConsumer.commitEveryNOffsets, config.kafkaConsumer.commitTimeWindow))
      }.parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: Applicative: WithRun[*[_], I, AppContext],
    S[_]: LiftStream[*[_], I]
  ](config: ParserConfig)(
    parse: Parse[F],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], ParserCommand.ParseOzonResponse]
  ): Resource[I, Parser[S]] =
    Resource.liftF {
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
