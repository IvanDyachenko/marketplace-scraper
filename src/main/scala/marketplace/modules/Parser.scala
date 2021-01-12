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
import marketplace.models.{Command, Event}
import marketplace.models.parser.{ParserCommand, ParserEvent}

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {

  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  def make[I[_]: Monad: Concurrent: Timer, F[_]: WithRun[*[_], I, AppContext], S[_]: LiftStream[*[_], I]](config: ParserConfig)(
    parse: Parse[F],
    consumer: KafkaConsumer[I, Command.Key, ParserCommand],
    producer: KafkaProducer[I, Event.Key, ParserEvent]
  ): Resource[I, Parser[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Parser[Stream[I, *]] = new Parser[Stream[I, *]] {
            def run: Stream[I, Unit] =
              consumer.partitionedStream.map { partition =>
                partition
                  .parEvalMap(config.maxConcurrent) { committable =>
                    runContext(parse.handle(committable.record.value))(AppContext()).map(_ -> committable.offset)
                  }
                  .map { case (event, offset) => ProducerRecords.one(ProducerRecord(config.eventsTopic, event.key, event), offset) }
                  .evalMap(producer.produce)
                  .parEvalMap(1000)(identity)
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
