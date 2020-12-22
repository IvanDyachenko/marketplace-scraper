package marketplace.modules

import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import cats.tagless.syntax.functorK._
import tofu.WithRun
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import derevo.derive
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream
import fs2.kafka.{commitBatchWithin, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}

import marketplace.config.ParserConfig
import marketplace.context.AppContext
import marketplace.services.Parse
import marketplace.models.{CommandKey, EventKey}
import marketplace.models.parser.{Command, Event}

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {

  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  def make[I[_]: Monad: Concurrent: Timer, F[_]: WithRun[*[_], I, AppContext], S[_]: LiftStream[*[_], I]](
    parse: Parse[F],
    consumer: KafkaConsumer[I, CommandKey, Command],
    producer: KafkaProducer[I, EventKey, Event]
  )(config: ParserConfig): Resource[I, Parser[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Parser[Stream[I, *]] = new Parser[Stream[I, *]] {
            def run: Stream[I, Unit] =
              consumer.partitionedStream.map { partition =>
                partition
                  .evalMap { committable =>
                    runContext(parse.handle(committable.record.value))(AppContext()).map { event =>
                      ProducerRecords.one(
                        ProducerRecord(config.eventsTopic, event.key, event),
                        committable.offset
                      )
                    }
                  }
                  .through(_.evalMap(producer.produce).mapAsync(1000)(identity))
                  .map(_.passthrough)
                  .through(commitBatchWithin(config.batchOffsets, config.batchTimeWindow))
              }.parJoinUnbounded
          }

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
