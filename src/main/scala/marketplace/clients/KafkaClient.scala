package marketplace.clients

import scala.concurrent.duration._

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import fs2.kafka.{consumerResource, AutoOffsetReset, ConsumerSettings, KafkaConsumer, KafkaProducer, ProducerSettings, RecordDeserializer, RecordSerializer}
import vulcan.Codec
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}

import marketplace.config.{KafkaConfig, SchemaRegistryConfig}

object KafkaClient {

  def makeProducer[F[_]: ContextShift: Concurrent, K: Codec, V: Codec](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig
  ): Resource[F, KafkaProducer[F, K, V]] = {
    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

    val keySerializer: RecordSerializer[F, K]   = avroSerializer[K].using(avroSettings)
    val valueSerializer: RecordSerializer[F, V] = avroSerializer[V].using(avroSettings)

    val producerSettings = ProducerSettings[F, K, V](keySerializer, valueSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withLinger(500 milliseconds)
      .withBatchSize(1536000)
      .withProperty("compression.type", "zstd")

    KafkaProducer.resource[F, K, V](producerSettings)
  }

  def makeConsumer[F[_]: ContextShift: ConcurrentEffect: Timer, K: Codec, V: Codec](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig
  )(groupId: String, topic: String): Resource[F, KafkaConsumer[F, K, V]] = {
    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

    val keyDeserializer: RecordDeserializer[F, K]   = avroDeserializer[K].using(avroSettings)
    val valueDeserializer: RecordDeserializer[F, V] = avroDeserializer[V].using(avroSettings)

    val consumerSettings = ConsumerSettings[F, K, V](keyDeserializer, valueDeserializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)

    consumerResource[F, K, V](consumerSettings).evalTap(_.subscribeTo(topic))
  }
}
