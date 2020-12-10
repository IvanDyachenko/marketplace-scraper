package marketplace.modules

import cats.{FlatMap, Functor}
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
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
import io.circe.parser.decode
import vulcan.{AvroError, Codec}
import fs2.kafka.{consumerStream, AutoOffsetReset, ConsumerSettings, KafkaConsumer}
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerResult, ProducerSettings}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}
import fs2.kafka.{RecordDeserializer, RecordSerializer}
import java.nio.charset.StandardCharsets.UTF_8

import marketplace.marshalling._
import marketplace.config.SchemaRegistryConfig
import marketplace.services.Crawl
import marketplace.models.yandex.market.Request

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[ProducerResult[String, Json, Unit]]
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

          implicit val jsonCodec: Codec[Json] =
            Codec.bytes.imapError(bytes => decode[Json](new String(bytes, UTF_8)).left.map(err => AvroError(err.getMessage)))(
              _.noSpaces.getBytes(UTF_8)
            )

          implicit val responseSerializer: RecordSerializer[F, Json]       = avroSerializer[Json].using(avroSettings)
          implicit val requestDeserializer: RecordDeserializer[F, Request] = avroDeserializer[Request].using(avroSettings)

          val consumerSettings = ConsumerSettings[F, String, Request]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers("http://localhost:9092")
            .withGroupId("crawler")
          val consumer         = consumerStream[F].using(consumerSettings)

          val producerSettings = ProducerSettings[F, String, Json]
            .withBootstrapServers("http://localhost:9092")
          val producer         = KafkaProducer.stream[F].using(producerSettings)

          val crawler: Crawler[Stream[F, *]] = new Impl[F](consumer, producer, producerSettings, crawl)

          crawler.pure[F]
        })
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[I]
    )

  private final class Impl[F[_]: Functor: Concurrent](
    consumer: Stream[F, KafkaConsumer[F, String, Request]],
    producer: Stream[F, KafkaProducer[F, String, Json]],
    producerSettings: ProducerSettings[F, String, Json],
    crawl: Crawl[Stream[F, *]]
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, ProducerResult[String, Json, Unit]] =
      producer.flatMap { producer =>
        consumer
          .evalTap(_.subscribeTo("crawler-commands-requests"))
          .flatMap(_.partitionedStream)
          .map { partition =>
            partition
              .map(_.record.value.toHttpRequest)
              .through(crawl.crawl[Request, Json])
              .map(httpResponse => ProducerRecords.one(ProducerRecord("crawler-events-responses", "key", httpResponse.result)))
              .through(KafkaProducer.pipe[F, String, Json, Unit](producerSettings, producer))
          }
          .parJoinUnbounded
      }
  }
}
