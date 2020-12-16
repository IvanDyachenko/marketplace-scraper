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
import fs2.{Pipe, Stream}
import tofu.fs2.LiftStream
import fs2.kafka.{commitBatchWithin, consumerStream => KafkaConsumerStream, AutoOffsetReset, CommittableConsumerRecord, ConsumerSettings, KafkaConsumer}
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings}
import fs2.kafka.{RecordDeserializer, RecordSerializer}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}

import marketplace.config.{CrawlerConfig, KafkaConfig, SchemaRegistryConfig}
import marketplace.models.{CommandKey, EventKey}
import marketplace.models.crawler.{Command, Event}
import marketplace.services.Crawl

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  private final class Impl[F[_]: Monad: Concurrent: Timer](eventsTopic: String, commandsTopic: String)(
    crawl: Crawl[F],
    consumerStream: Stream[F, KafkaConsumer[F, CommandKey, Command]],
    producerStream: Stream[F, KafkaProducer[F, EventKey, Event]],
    producerSettings: ProducerSettings[F, EventKey, Event],
    batchOffsets: Int,
    batchTimeWindow: FiniteDuration
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      producerStream.flatMap { producer =>
        consumerStream
          .evalTap(_.subscribeTo(commandsTopic))
          .flatMap(_.partitionedStream)
          .map(_.through(handle(producer)))
          .parJoinUnbounded
      }

    private def handle(producer: KafkaProducer[F, EventKey, Event]): Pipe[F, CommittableConsumerRecord[F, CommandKey, Command], Unit] =
      _.evalMap { committable =>
        crawl
          .handle(committable.record.value)
          .map(event => ProducerRecords.one(ProducerRecord(eventsTopic, event.key, event), committable.offset))
      }
        .through(KafkaProducer.pipe(producerSettings, producer))
        .map(_.passthrough)
        .through(commitBatchWithin(batchOffsets, batchTimeWindow))
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

          implicit val eventSerializer: RecordSerializer[F, Event]               = avroSerializer[Event].using(avroSettings)
          implicit val eventKeySerializer: RecordSerializer[F, EventKey]         = avroSerializer[EventKey].using(avroSettings)
          implicit val commandDeserializer: RecordDeserializer[F, Command]       = avroDeserializer[Command].using(avroSettings)
          implicit val commandKeyDeserializer: RecordDeserializer[F, CommandKey] = avroDeserializer[CommandKey].using(avroSettings)

          val producerSettings = ProducerSettings[F, EventKey, Event]
            .withBootstrapServers(kafkaConfig.bootstrapServers)
          val consumerSettings = ConsumerSettings[F, CommandKey, Command]
            .withBootstrapServers(kafkaConfig.bootstrapServers)
            .withGroupId(crawlerConfig.groupId)
            .withAutoOffsetReset(AutoOffsetReset.Earliest)

          val consumerStream = KafkaConsumerStream[F].using(consumerSettings)
          val producerStream = KafkaProducer.stream[F].using(producerSettings)

          val crawler: Crawler[Stream[F, *]] =
            new Impl[F](eventsTopic, commandsTopic)(crawl, consumerStream, producerStream, producerSettings, batchOffsets, batchTimeWindow)

          crawler.pure[F]
        })
        .embed
        .mapK(LiftStream[S, F].liftF)
        .pure[I]
    )
}
