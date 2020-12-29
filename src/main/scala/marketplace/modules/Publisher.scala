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
trait Publisher[S[_]] {
  def run: S[Unit]
}

object Publisher {

  def apply[S[_]](implicit ev: Publisher[S]): ev.type = ev

  def make[I[_]: Monad: Concurrent: Timer, S[_]: LiftStream[*[_], I], K, V](
    sources: List[Stream[I, (K, V)]],
    topic: String,
    producer: KafkaProducer[I, K, V]
  ): Resource[I, Publisher[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Publisher[Stream[I, *]] = new Publisher[Stream[I, *]] {

            def run: Stream[I, Unit] =
              Stream
                .emits(sources)
                .parJoinUnbounded
                .evalMap { case (key, value) => producer.produce(ProducerRecords.one(ProducerRecord(topic, key, value))) }
                .parEvalMap(1000)(identity)
                .map(_.passthrough)
          }

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
