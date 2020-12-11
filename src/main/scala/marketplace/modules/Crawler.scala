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
import marketplace.models.CrawlerCommand
import marketplace.services.HandleCrawlerCommand

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  private final class Impl[F[_]: Monad: Concurrent: Timer](
    commandHandler: HandleCrawlerCommand[F],
    consumer: Stream[F, KafkaConsumer[F, String, CrawlerCommand]]
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      consumer
        .evalTap(_.subscribeTo("crawler-commands-marketplace_requests"))
        .flatMap(_.partitionedStream)
        .map { partition =>
          partition
            .evalMap { commitable =>
              commandHandler.handle(commitable.record.value).as(commitable.offset)
            }
            .through(commitBatchWithin(100, 5.seconds))
        }
        .parJoinUnbounded
        .map(_ => ())
  }

  def apply[S[_]](implicit ev: Crawler[S]): ev.type = ev

  def make[I[_]: ConcurrentEffect: Unlift[*[_], F], F[_]: FlatMap: ContextShift: Timer, S[_]: LiftStream[*[_], F]](
    config: SchemaRegistryConfig,
    commandHandler: HandleCrawlerCommand[F]
  ): Resource[I, Crawler[S]] =
    Resource.liftF(
      Stream
        .eval(Unlift[I, F].concurrentEffectWith { implicit ce =>
          val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](config.baseUrl))

          implicit val requestDeserializer: RecordDeserializer[F, CrawlerCommand] = avroDeserializer[CrawlerCommand].using(avroSettings)

          val consumerSettings = ConsumerSettings[F, String, CrawlerCommand]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers("http://localhost:9092")
            .withGroupId("crawler")
          val consumer         = consumerStream[F].using(consumerSettings)

          val crawler: Crawler[Stream[F, *]] = new Impl[F](commandHandler, consumer)

          crawler.pure[F]
        })
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[I]
    )
}
