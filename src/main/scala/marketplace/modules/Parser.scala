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
import fs2.kafka.{commitBatchWithin, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}

import marketplace.config.ParserConfig
import marketplace.context.AppContext
import marketplace.services.Parse
import marketplace.models.{ozon, Command, Event}
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
    producerOfEvents: KafkaProducer[I, Event.Key, ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Command.Key, ParserCommand.ParseOzonResponse]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.maxConcurrent) { committable =>
            runContext(parse.handle(committable.record.value))(AppContext()).map(_.toOption.map(_ -> committable.offset))
          }
          .collect { case Some((ParserEvent.OzonResponseParsed(id, key, created, time, ozon.Result.SearchResultsV2(items)), offset)) =>
            val events = items.map(ParserEvent.ozonItemParsed(id, key, created, time, _))
            ProducerRecords(events.map(ProducerRecord(config.ozonItemsTopic, key, _)), offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(config.maxConcurrent)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.batchOffsets, config.batchTimeWindow))
      }.parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: Applicative: WithRun[*[_], I, AppContext],
    S[_]: LiftStream[*[_], I]
  ](config: ParserConfig)(
    parse: Parse[F],
    producerOfEvents: KafkaProducer[I, Event.Key, ParserEvent],
    consumerOfCommands: KafkaConsumer[I, Command.Key, ParserCommand.ParseOzonResponse]
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
