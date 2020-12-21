package marketplace.sources

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import fs2.kafka.{consumerResource, AutoOffsetReset, ConsumerSettings, KafkaConsumer, KafkaProducer, ProducerSettings, RecordDeserializer, RecordSerializer}
import vulcan.Codec
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}

import marketplace.config.{KafkaConfig, SchemaRegistryConfig}

object KafkaSource {

  def makeProducer[F[_]: ContextShift: Concurrent, K: Codec, V: Codec](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig
  ): Resource[F, KafkaProducer[F, K, V]] = {
    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

    implicit val keySerializer: RecordSerializer[F, K]   = avroSerializer[K].using(avroSettings)
    implicit val valueSerializer: RecordSerializer[F, V] = avroSerializer[V].using(avroSettings)

    val producerSettings = ProducerSettings[F, K, V]
      .withBootstrapServers(kafkaConfig.bootstrapServers)

    KafkaProducer.resource[F, K, V](producerSettings)
  }

  def makeConsumer[F[_]: ContextShift: ConcurrentEffect: Timer, K: Codec, V: Codec](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig
  )(groupId: String, topic: String): Resource[F, KafkaConsumer[F, K, V]] = {
    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

    implicit val keyDeserializer: RecordDeserializer[F, K]   = avroDeserializer[K].using(avroSettings)
    implicit val valueDeserializer: RecordDeserializer[F, V] = avroDeserializer[V].using(avroSettings)

    val consumerSettings = ConsumerSettings[F, K, V]
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)

    consumerResource[F, K, V](consumerSettings).evalTap(_.subscribeTo(topic))
  }
}
