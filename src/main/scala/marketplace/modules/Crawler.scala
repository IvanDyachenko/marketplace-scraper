package marketplace.modules

import cats.{FlatMap, Functor, Inject}
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Timer}
import cats.tagless.syntax.functorK._
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.lift.Unlift
import tofu.syntax.unlift._
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream
import io.circe.Json
import fs2.kafka.{consumerStream, AutoOffsetReset, ConsumerSettings, KafkaConsumer}
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerResult, ProducerSettings}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}
import fs2.kafka.{RecordDeserializer, RecordSerializer}

import marketplace.config.SchemaRegistryConfig
import marketplace.services.Crawl
import marketplace.clients.models.schema._
import marketplace.clients.models.{HttpRequest, HttpResponse}
import cats.effect.Concurrent

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[ProducerResult[String, HttpResponse[Json], Unit]]
}

object Crawler extends ContextEmbed[Crawl] {
  def apply[S[_]](implicit ev: Crawler[S]): ev.type = ev

  def make[I[_]: ConcurrentEffect: Unlift[*[_], F], F[_]: FlatMap: ContextShift: Timer, S[_]: LiftStream[*[_], F]](
    config: SchemaRegistryConfig,
    crawl: Crawl[Stream[F, *]]
  ): Resource[I, Crawler[S]] =
    Resource.liftF(
      Stream
        .eval(Unlift[I, F].concurrentEffectWith { implicit ce =>
          val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](config.baseUrl))

          implicit def httpResponseSerializer[R: Inject[*, Array[Byte]]]: RecordSerializer[F, HttpResponse[R]]   =
            avroSerializer[HttpResponse[R]].using(avroSettings)
          implicit def httpRequestDeserializer[R: Inject[*, Array[Byte]]]: RecordDeserializer[F, HttpRequest[R]] =
            avroDeserializer[HttpRequest[R]].using(avroSettings)

          val producerSettings = ProducerSettings[F, String, HttpResponse[Json]].withBootstrapServers("broker:9092")
          val consumerSettings = ConsumerSettings[F, String, HttpRequest[Json]]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers("broker:29092")
            .withGroupId("group")

          val producer = KafkaProducer.stream[F].using(producerSettings)
          val consumer = consumerStream[F].using(consumerSettings)

          val crawler: Crawler[Stream[F, *]] = new Impl[F](consumer, producer, producerSettings, crawl)

          crawler.pure[F]
        })
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[I]
    )

  private final class Impl[F[_]: Functor: Concurrent](
    consumer: Stream[F, KafkaConsumer[F, String, HttpRequest[Json]]],
    producer: Stream[F, KafkaProducer[F, String, HttpResponse[Json]]],
    producerSettings: ProducerSettings[F, String, HttpResponse[Json]],
    crawl: Crawl[Stream[F, *]]
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, ProducerResult[String, HttpResponse[Json], Unit]] =
      producer.flatMap { producer =>
        consumer
          .evalTap(_.subscribeTo("topic"))
          .flatMap(_.partitionedStream)
          .map { partition =>
            partition
              .map(_.record.value)
              .through(crawl.crawl[Json, Json])
              .map(httpResponse => ProducerRecords.one(ProducerRecord("topic", "key", httpResponse)))
              .through(KafkaProducer.pipe[F, String, HttpResponse[Json], Unit](producerSettings, producer))
          }
          .parJoinUnbounded
      }
  }
}
