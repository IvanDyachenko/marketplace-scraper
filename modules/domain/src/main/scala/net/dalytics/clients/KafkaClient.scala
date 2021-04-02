package net.dalytics.clients

import tofu.syntax.monadic._
import cats.effect.{ConcurrentEffect, Resource}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer, KafkaConsumer, KafkaProducer, ProducerSettings, RecordDeserializer, RecordSerializer, Serializer}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings}
import vulcan.{Codec => VulcanCodec}

import net.dalytics.config.{KafkaConfig, KafkaConsumerConfig, KafkaProducerConfig}
import cats.effect.Temporal

object KafkaClient {

  def makeProducer[F[_]: ContextShift: ConcurrentEffect, K: VulcanCodec, V: VulcanCodec](
    kafkaConfig: KafkaConfig,
    kafkaProducerConfig: KafkaProducerConfig
  )(schemaRegistryClient: SchemaRegistryClient): Resource[F, KafkaProducer[F, Option[K], V]] = {
    val avroSettings = AvroSettings(schemaRegistryClient)
    // Setting auto.register.schemas to false disables
    // auto-registration of the event type, so that it does not
    // override the union as the latest schema in the subject.
    //.withAutoRegisterSchemas(false)
    // Setting use.latest.version to true сauses the Avro serializer
    // to look up the latest schema version in the subject(which
    // will be the union) and use that for serialization. Otherwise,
    // if set to false, the serializer will look for the event type
    // in the subject and fail to find it.
    //.withProperty("use.latest.version", "true")

    val keySerializer: RecordSerializer[F, Option[K]] =
      RecordSerializer.const(avroSerializer[K].using(avroSettings).forKey.map(implicit ser => Serializer.option[F, K]))

    val valueSerializer: RecordSerializer[F, V] =
      avroSerializer[V].using(avroSettings)

    val producerSettings = ProducerSettings[F, Option[K], V](keySerializer, valueSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withLinger(kafkaProducerConfig.linger)
      .withBatchSize(kafkaProducerConfig.batchSize)
      .withParallelism(kafkaProducerConfig.maxBufferSize)
      .withProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducerConfig.compressionType)
      .withProperty("confluent.monitoring.interceptor.bootstrap.servers", kafkaConfig.bootstrapServers)
      .withProperty(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor")

    KafkaProducer.resource[F, Option[K], V](producerSettings)
  }

  def makeConsumer[F[_]: ContextShift: ConcurrentEffect: Temporal, K: VulcanCodec, V: VulcanCodec](
    kafkaConfig: KafkaConfig,
    kafkaConsumerConfig: KafkaConsumerConfig
  )(schemaRegistryClient: SchemaRegistryClient): Resource[F, KafkaConsumer[F, Option[K], V]] = {
    val avroSettings = AvroSettings(schemaRegistryClient)
    // Setting auto.register.schemas to false disables
    // auto-registration of the event type, so that it does not
    // override the union as the latest schema in the subject.
    //.withAutoRegisterSchemas(false)
    // Setting use.latest.version to true сauses the Avro serializer
    // to look up the latest schema version in the subject(which
    // will be the union) and use that for serialization. Otherwise,
    // if set to false, the serializer will look for the event type
    // in the subject and fail to find it.
    //.withProperty("use.latest.version", "true")

    val keyDeserializer: RecordDeserializer[F, Option[K]] =
      RecordDeserializer.const(avroDeserializer[K].using(avroSettings).forKey.map(implicit der => Deserializer.option[F, K]))

    val valueDeserializer: RecordDeserializer[F, V] =
      avroDeserializer[V].using(avroSettings)

    val consumerSettingsBase =
      ConsumerSettings[F, Option[K], V](keyDeserializer, valueDeserializer)
        .withBootstrapServers(kafkaConfig.bootstrapServers)
        .withGroupId(kafkaConsumerConfig.groupId)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withProperty("confluent.monitoring.interceptor.bootstrap.servers", kafkaConfig.bootstrapServers)
        .withProperty(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor")

    val consumerSettingsWithFetchMaxBytes =
      kafkaConsumerConfig.fetchMaxBytes
        .fold(consumerSettingsBase)(fetchMaxBytes => consumerSettingsBase.withProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, fetchMaxBytes.toString))

    val consumerSettingsWithMaxPartitionFetchBytes =
      kafkaConsumerConfig.maxPartitionFetchBytes
        .fold(consumerSettingsWithFetchMaxBytes)(maxPartitionFetchBytes =>
          consumerSettingsWithFetchMaxBytes.withProperty(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, maxPartitionFetchBytes.toString)
        )

    val consumerSettingsWithCommitTimeout =
      kafkaConsumerConfig.commitTimeout
        .fold(consumerSettingsWithMaxPartitionFetchBytes)(consumerSettingsWithMaxPartitionFetchBytes.withCommitTimeout)

    val consumerSettingsWithMaxPollRecords =
      kafkaConsumerConfig.maxPollRecords
        .fold(consumerSettingsWithCommitTimeout)(consumerSettingsWithCommitTimeout.withMaxPollRecords)

    val consumerSettingsWithMaxPollInterval =
      kafkaConsumerConfig.maxPollInterval
        .fold(consumerSettingsWithMaxPollRecords)(consumerSettingsWithMaxPollRecords.withMaxPollInterval)

    val consumerSettingsWithEnableAutoCommit =
      kafkaConsumerConfig.enableAutoCommit
        .fold(consumerSettingsWithMaxPollInterval)(consumerSettingsWithMaxPollInterval.withEnableAutoCommit)

    KafkaConsumer.resource[F, Option[K], V](consumerSettingsWithEnableAutoCommit).evalTap(_.subscribeTo(kafkaConsumerConfig.topic))
  }
}
