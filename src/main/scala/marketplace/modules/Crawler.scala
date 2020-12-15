package marketplace.modules

import scala.concurrent.duration.FiniteDuration

import cats.implicits._
import cats.{FlatMap, Monad}
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.tagless.syntax.functorK._
import tofu.syntax.embed._
import tofu.lift.Unlift
import tofu.syntax.unlift._
import derevo.derive
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream
import fs2.kafka.{commitBatchWithin, consumerStream, AutoOffsetReset, ConsumerSettings, KafkaConsumer}
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings}
import fs2.kafka.{RecordDeserializer, RecordSerializer}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}

import marketplace.config.{CrawlerConfig, KafkaConfig, SchemaRegistryConfig}
import marketplace.models.crawler.{Command, Event}
import marketplace.services.Crawl
import fs2.kafka.CommittableOffset

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  private final class Impl[F[_]: Monad: Concurrent: Timer](
    crawl: Crawl[F],
    consumerS: Stream[F, KafkaConsumer[F, String, Command]],
    producerS: Stream[F, KafkaProducer[F, String, Event]],
    producerSettings: ProducerSettings[F, String, Event],
    eventsTopic: String,
    commandsTopic: String,
    batchOffsets: Int,
    batchTimeWindow: FiniteDuration
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      producerS.flatMap { producer =>
        consumerS
          .evalTap(_.subscribeTo(commandsTopic))
          .flatMap(_.partitionedStream)
          .map { partition =>
            partition
              .evalMap { committable =>
                crawl
                  .handle(committable.record.value)
                  .flatTap { event =>
                    val record  = ProducerRecord(eventsTopic, event.key.show, event)
                    val records = ProducerRecords.one(record, committable.offset)

                    Stream
                      .emit[F, ProducerRecords[String, Event, CommittableOffset[F]]](records)
                      .through(KafkaProducer.pipe(producerSettings, producer))
                      .compile
                      .drain
                  }
                  .as(committable.offset)
              }
              .through(commitBatchWithin(batchOffsets, batchTimeWindow))
          }
          .parJoinUnbounded
      }
  }

  def apply[S[_]](implicit ev: Crawler[S]): ev.type = ev

  def make[I[_]: ConcurrentEffect: Unlift[*[_], F], F[_]: FlatMap: ContextShift: Timer, S[_]: LiftStream[*[_], F]](
    crawlerConfig: CrawlerConfig,
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig,
    crawl: Crawl[F]
  ): Resource[I, Crawler[S]] =
    Resource.liftF(
      Stream
        .eval(Unlift[I, F].concurrentEffectWith { implicit ce =>
          val CrawlerConfig(groupId, eventsTopic, commandsTopic, batchOffsets, batchTimeWindow) = crawlerConfig

          val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

          implicit val eventSerializer: RecordSerializer[F, Event]     = avroSerializer[Event].using(avroSettings)
          implicit val eventDeserializer: RecordDeserializer[F, Event] = avroDeserializer[Event].using(avroSettings)

          val producerSettings = ProducerSettings[F, String, Event]
            .withBootstrapServers(kafkaConfig.bootstrapServers)
          val producer         = KafkaProducer.stream[F].using(producerSettings)

          implicit val commandSerializer: RecordSerializer[F, Command]     = avroSerializer[Command].using(avroSettings)
          implicit val commandDeserializer: RecordDeserializer[F, Command] = avroDeserializer[Command].using(avroSettings)

          val consumerSettings = ConsumerSettings[F, String, Command]
            .withAutoOffsetReset(AutoOffsetReset.Earliest)
            .withBootstrapServers(kafkaConfig.bootstrapServers)
            .withGroupId(crawlerConfig.groupId)
          val consumer         = consumerStream[F].using(consumerSettings)

          val crawler: Crawler[Stream[F, *]] =
            new Impl[F](crawl, consumer, producer, producerSettings, eventsTopic, commandsTopic, batchOffsets, batchTimeWindow)

          crawler.pure[F]
        })
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[I]
    )
}
