package marketplace.modules

import scala.concurrent.duration._

import cats.{FlatMap, Monad}
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.tagless.syntax.functorK._
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.lift.Unlift
import tofu.syntax.unlift._
import derevo.derive
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream
import fs2.kafka.{commitBatchWithin, consumerStream, AutoOffsetReset, ConsumerSettings, KafkaConsumer}
import fs2.kafka.RecordDeserializer
import fs2.kafka.vulcan.{avroDeserializer, AvroSettings, SchemaRegistryClientSettings}

import marketplace.config.SchemaRegistryConfig
import marketplace.models.crawler.Command
import marketplace.services.Crawl

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  private final class Impl[F[_]: Monad: Concurrent: Timer](
    crawl: Crawl[F],
    consumer: Stream[F, KafkaConsumer[F, String, Command]]
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      consumer
        .evalTap(_.subscribeTo("crawler-commands-handle_yandex_market_request"))
        .flatMap(_.partitionedStream)
        .map { partition =>
          partition
            .evalMap(commitable => crawl.handle(commitable.record.value).as(commitable.offset))
            .through(commitBatchWithin(100, 5.seconds))
        }
        .parJoinUnbounded
        .map(_ => ())
  }

  def apply[S[_]](implicit ev: Crawler[S]): ev.type = ev

  def make[I[_]: ConcurrentEffect: Unlift[*[_], F], F[_]: FlatMap: ContextShift: Timer, S[_]: LiftStream[*[_], F]](
    config: SchemaRegistryConfig,
    crawl: Crawl[F]
  ): Resource[I, Crawler[S]] =
    Resource.liftF(
      Stream
        .eval(Unlift[I, F].concurrentEffectWith { implicit ce =>
          val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](config.baseUrl))

          implicit val requestDeserializer: RecordDeserializer[F, Command] = avroDeserializer[Command].using(avroSettings)

          val consumerSettings = ConsumerSettings[F, String, Command]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers("http://localhost:9092")
            .withGroupId("crawler")
          val consumer         = consumerStream[F].using(consumerSettings)

          val crawler: Crawler[Stream[F, *]] = new Impl[F](crawl, consumer)

          crawler.pure[F]
        })
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[I]
    )
}
