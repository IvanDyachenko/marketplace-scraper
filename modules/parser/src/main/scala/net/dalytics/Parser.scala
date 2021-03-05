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
import net.dalytics.context.CommandContext
import net.dalytics.services.Parse
import net.dalytics.models.{Command, Event}
import net.dalytics.models.parser.{ParserCommand, ParserEvent}

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: Applicative: WithRun[*[_], I, CommandContext]
  ](config: Config)(
    parse: Parse[F],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], ParserCommand.ParseOzonResponse]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.kafkaConsumerConfig.maxConcurrentPerTopic) { committable =>
            runContext(parse.handle(committable.record.value))(CommandContext()).map(
              _.toOption.fold[(List[ParserEvent], CommittableOffset[I])](List.empty -> committable.offset)(_ -> committable.offset)
            )
          }
          .collect { case (events, offset) =>
            ProducerRecords(events.map(event => ProducerRecord(config.kafkaProducerConfig.topic, event.key, event)), offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(config.kafkaProducerConfig.maxBufferSize)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.kafkaConsumerConfig.commitEveryNOffsets, config.kafkaConsumerConfig.commitTimeWindow))
      }.parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: Applicative: WithRun[*[_], I, CommandContext],
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
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
