package marketplace.clients

import tofu.syntax.monadic._
import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import fs2.kafka.{consumerResource, AutoOffsetReset, ConsumerSettings, Deserializer, KafkaConsumer, KafkaProducer, ProducerSettings, RecordDeserializer, RecordSerializer, Serializer}
import vulcan.Codec
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings, SchemaRegistryClientSettings}

import marketplace.config.{KafkaConfig, KafkaConsumerConfig, KafkaProducerConfig, SchemaRegistryConfig}

object KafkaClient {

  def makeProducer[F[_]: ContextShift: Concurrent, K: Codec, V: Codec](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig,
    kafkaProducerConfig: KafkaProducerConfig
  ): Resource[F, KafkaProducer[F, Option[K], V]] = {
    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

    val keySerializer: RecordSerializer[F, Option[K]] =
      RecordSerializer.const(avroSerializer[K].using(avroSettings).forKey.map(implicit ser => Serializer.option[F, K]))
    val valueSerializer: RecordSerializer[F, V]       = avroSerializer[V].using(avroSettings)

    val producerSettings = ProducerSettings[F, Option[K], V](keySerializer, valueSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withLinger(kafkaProducerConfig.linger)
      .withBatchSize(kafkaProducerConfig.batchSize)
      .withProperty("compression.type", kafkaProducerConfig.compressionType)

    KafkaProducer.resource[F, Option[K], V](producerSettings)
  }

  def makeConsumer[F[_]: ContextShift: ConcurrentEffect: Timer, K: Codec, V: Codec](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig,
    kafkaConsumerConfig: KafkaConsumerConfig
  ): Resource[F, KafkaConsumer[F, Option[K], V]] = {
    val avroSettings = AvroSettings(SchemaRegistryClientSettings[F](schemaRegistryConfig.baseUrl))

    val keyDeserializer: RecordDeserializer[F, Option[K]] =
      RecordDeserializer.const(avroDeserializer[K].using(avroSettings).forKey.map(implicit der => Deserializer.option[F, K]))
    val valueDeserializer: RecordDeserializer[F, V]       = avroDeserializer[V].using(avroSettings)

    val consumerSettings1 = ConsumerSettings[F, Option[K], V](keyDeserializer, valueDeserializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withGroupId(kafkaConsumerConfig.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withProperty("connections.max.idle.ms", "-1")
    val consumerSettings2 = kafkaConsumerConfig.commitTimeout.fold(consumerSettings1)(consumerSettings1.withCommitTimeout)
    val consumerSettings3 = kafkaConsumerConfig.maxPollRecords.fold(consumerSettings2)(consumerSettings2.withMaxPollRecords)
    val consumerSettings4 = kafkaConsumerConfig.maxPollInterval.fold(consumerSettings3)(consumerSettings3.withMaxPollInterval)

    consumerResource[F, Option[K], V](consumerSettings4).evalTap(_.subscribeTo(kafkaConsumerConfig.topic))
  }
}
