package marketplace.modules

import cats.tagless.syntax.functorK._
import cats.{Applicative, Monad}
import cats.effect.{Concurrent, Resource, Timer}
import tofu.syntax.embed._
//import tofu.syntax.handle._
import tofu.syntax.monadic._
import tofu.syntax.context._
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.WithRun
import tofu.fs2.LiftStream
import fs2.Stream
//import fs2.kafka.{commitBatchWithin, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}
import fs2.kafka.{commitBatchWithin, KafkaConsumer}

import marketplace.config.ParserConfig
import marketplace.context.AppContext
import marketplace.services.Parse
import marketplace.services.Parse.ParsingError
import marketplace.models.Command
import marketplace.models.parser.ParserCommand

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: Applicative: WithRun[*[_], I, AppContext]: ParsingError.Handling
  ](config: ParserConfig)(
    parse: Parse[F],
    consumerOfParserCommands: KafkaConsumer[I, Command.Key, ParserCommand.ParseOzonResponse]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfParserCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.maxConcurrent) { committable =>
            runContext(parse.handle(committable.record.value))(AppContext()).as(committable.offset)
          }
          .through(commitBatchWithin(config.batchOffsets, config.batchTimeWindow))
      }.parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: Applicative: WithRun[*[_], I, AppContext]: ParsingError.Handling,
    S[_]: LiftStream[*[_], I]
  ](config: ParserConfig)(
    parse: Parse[F],
    consumerOfParserCommands: KafkaConsumer[I, Command.Key, ParserCommand.ParseOzonResponse]
  ): Resource[I, Parser[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Parser[Stream[I, *]] = new Impl[I, F](config)(parse, consumerOfParserCommands)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
