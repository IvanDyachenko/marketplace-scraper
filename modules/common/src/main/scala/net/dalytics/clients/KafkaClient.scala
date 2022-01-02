package net.dalytics.clients

import java.util.Properties
import scala.reflect.ClassTag

import tofu.syntax.monadic._
import cats.effect.{ConcurrentEffect, Resource, Sync}
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler
import org.apache.kafka.streams.processor.TimestampExtractor
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.consumer.ConsumerConfig
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer, KafkaConsumer, KafkaProducer, ProducerSettings, RecordDeserializer, RecordSerializer, Serializer}
import fs2.kafka.vulcan.{avroDeserializer, avroSerializer, AvroSettings}
import vulcan.{Codec => VulcanCodec}

import net.dalytics.config.{KafkaConfig, KafkaConsumerConfig, KafkaProducerConfig, KafkaStreamsConfig, SchemaRegistryConfig}
import cats.effect.Temporal

object KafkaClient {

  def makeAvroSettings[F[_]: Sync](schemaRegistryClient: SchemaRegistryClient): AvroSettings[F] =
    AvroSettings(schemaRegistryClient)
  // Setting auto.register.schemas to false disables
  // auto-registration of the event type, so that it does not
  // override the union as the latest schema in the subject.
  //.withAutoRegisterSchemas(false)
  // Setting use.latest.version to true сauses the Avro serializer
  // to look up the latest schema version in the subject(which
  // will be the union) and use that for serialization. Otherwise,
  // if set to false, the serializer will look for the event type
  // in the subject and fail to find it.
  //.withProperty(KafkaAvroSerializerConfig.USE_LATEST_VERSION, "true")

  def makeProducer[F[_]: ContextShift: ConcurrentEffect, K: VulcanCodec, V: VulcanCodec](
    kafkaConfig: KafkaConfig,
    kafkaProducerConfig: KafkaProducerConfig
  )(schemaRegistryClient: SchemaRegistryClient): Resource[F, KafkaProducer[F, Option[K], V]] = {
    val avroSettings: AvroSettings[F] = makeAvroSettings(schemaRegistryClient)

    val keySerializer: RecordSerializer[F, Option[K]] =
      RecordSerializer.const(avroSerializer[K].using(avroSettings).forKey.map(implicit ser => Serializer.option[F, K]))

    val valueSerializer: RecordSerializer[F, V] =
      avroSerializer[V].using(avroSettings)

    val producerSettings = ProducerSettings[F, Option[K], V](keySerializer, valueSerializer)
      .withBootstrapServers(kafkaConfig.bootstrapServers)
      .withLinger(kafkaProducerConfig.linger)
      .withBatchSize(kafkaProducerConfig.batchSize)
      .withParallelism(kafkaProducerConfig.parallelism)
      .withProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaProducerConfig.compressionType)
      .withProperty("confluent.monitoring.interceptor.bootstrap.servers", kafkaConfig.bootstrapServers)
      .withProperty(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor")

    KafkaProducer.resource[F, Option[K], V](producerSettings)
  }

  def makeConsumer[F[_]: ContextShift: ConcurrentEffect: Temporal, K: VulcanCodec, V: VulcanCodec](
    kafkaConfig: KafkaConfig,
    kafkaConsumerConfig: KafkaConsumerConfig
  )(schemaRegistryClient: SchemaRegistryClient): Resource[F, KafkaConsumer[F, Option[K], V]] = {
    val avroSettings: AvroSettings[F] = makeAvroSettings(schemaRegistryClient)

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

  def makeKafkaStreamsConfiguration[T <: TimestampExtractor](
    kafkaConfig: KafkaConfig,
    schemaRegistryConfig: SchemaRegistryConfig,
    kafkaStreamsConfig: KafkaStreamsConfig
  )(implicit timestampExtractorClassTag: ClassTag[T]): Properties = {
    val streamsConfiguration = new Properties()

    streamsConfiguration.put(
      StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,
      kafkaConfig.bootstrapServers
    )
    streamsConfiguration.put(
      StreamsConfig.APPLICATION_ID_CONFIG,
      kafkaStreamsConfig.applicationId
    )
    streamsConfiguration.put(
      StreamsConfig.NUM_STREAM_THREADS_CONFIG,
      kafkaStreamsConfig.numberOfStreamThreads
    )
    streamsConfiguration.put(
      StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG,
      kafkaStreamsConfig.cacheMaxBytesBuffering
    )
    streamsConfiguration.put(
      StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
      kafkaStreamsConfig.commitInterval.toMillis
    )
    streamsConfiguration.put(
      StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
      classOf[LogAndFailExceptionHandler]
    )
    streamsConfiguration.put(
      StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG,
      timestampExtractorClassTag.runtimeClass
    )

//  streamsConfiguration.put(
//    AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS,
//    false
//  )
    streamsConfiguration.put(
      AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
      schemaRegistryConfig.url
    )

    streamsConfiguration.put(
      StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG),
      "earliest"
    )
    streamsConfiguration.put(
      StreamsConfig.consumerPrefix(ConsumerConfig.FETCH_MAX_BYTES_CONFIG),
      kafkaStreamsConfig.fetchMaxBytes
    )
    streamsConfiguration.put(
      StreamsConfig.consumerPrefix(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG),
      kafkaStreamsConfig.maxPartitionFetchBytes
    )
    streamsConfiguration.put(
      StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_RECORDS_CONFIG),
      kafkaStreamsConfig.maxPollRecords
    )

    streamsConfiguration.put(
      StreamsConfig.producerPrefix(ProducerConfig.BUFFER_MEMORY_CONFIG),
      kafkaStreamsConfig.bufferMemory
    )
    streamsConfiguration.put(
      StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG),
      kafkaStreamsConfig.compressionType
    )
    streamsConfiguration.put(
      StreamsConfig.producerPrefix(ProducerConfig.BATCH_SIZE_CONFIG),
      kafkaStreamsConfig.batchSize
    )
    streamsConfiguration.put(
      StreamsConfig.producerPrefix(ProducerConfig.LINGER_MS_CONFIG),
      kafkaStreamsConfig.linger.toMillis
    )

    streamsConfiguration.put(
      "confluent.monitoring.interceptor.bootstrap.servers",
      kafkaConfig.bootstrapServers
    )
    streamsConfiguration.put(
      StreamsConfig.mainConsumerPrefix(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG),
      "io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor"
    )
    streamsConfiguration.put(
      StreamsConfig.producerPrefix(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG),
      "io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor"
    )

    streamsConfiguration
  }
}
