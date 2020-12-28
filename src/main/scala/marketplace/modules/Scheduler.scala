package marketplace.modules

import cats.implicits._
import cats.tagless.syntax.functorK._
import tofu.syntax.embed._
import derevo.derive
import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import tofu.higherKind.derived.representableK
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords}

@derive(representableK)
trait Scheduler[S[_]] {
  def run: S[Unit]
}

object Scheduler {

  def apply[F[_]](implicit ev: Scheduler[F]): ev.type = ev

  def make[F[_]: Monad: Timer: Concurrent, S[_]: LiftStream[*[_], F], K, V](sources: List[Stream[F, (K, V)]])(
    topic: String,
    producer: KafkaProducer[F, K, V]
  ): Resource[F, Scheduler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Scheduler[Stream[F, *]] = new Scheduler[Stream[F, *]] {

            def run: Stream[F, Unit] =
              Stream
                .emits(sources)
                .parJoinUnbounded
                .evalMap { case (k, v) => producer.produce(ProducerRecords.one(ProducerRecord(topic, k, v))) }
                .parEvalMap(1000)(identity)
                .map(_.passthrough)
          }

          impl.pure[F]
        }
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[F]
    }
}
