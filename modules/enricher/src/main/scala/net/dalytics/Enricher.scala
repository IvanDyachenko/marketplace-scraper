package net.dalytics

import java.time.Duration
import java.util.Properties

import tofu.syntax.monadic._
import cats.effect.{Concurrent, Resource, Sync}
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.kstream.{Consumed, Grouped, Materialized, Produced, ValueMapper}
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import fs2.kafka.vulcan.AvroSettings
import compstak.kafkastreams4s.Platform

import net.dalytics.config.Config
import net.dalytics.serde.{VulcanSerde}
import net.dalytics.models.{ozon, Event}
import net.dalytics.models.parser.ParserEvent
import net.dalytics.models.enricher.EnricherEvent

trait Enricher[F[_]] {
  def run: F[Unit]
}

object Enricher {
  def apply[F[_]](implicit ev: Enricher[F]): ev.type = ev

  private final class Impl[F[_]: Concurrent](cfg: Config)(schemaRegistryClient: SchemaRegistryClient) extends Enricher[F] {
    def run: F[Unit] = {
      val streamsBuilder = new StreamsBuilder

      val streamsConfiguration: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, cfg.kafkaStreamsConfig.applicationId)
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.kafkaConfig.bootstrapServers)
        p.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, classOf[LogAndFailExceptionHandler])
        p.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, cfg.kafkaStreamsConfig.numberOfStreamThreads)
        p.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, cfg.kafkaStreamsConfig.commitInterval.toMillis)
        p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, cfg.kafkaStreamsConfig.cacheMaxBytesBuffering)
        //p.put(AbstractKafkaAvroSerDeConfig.AUTO_REGISTER_SCHEMAS, false)
        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, cfg.schemaRegistryConfig.baseUrl)
        p.put(StreamsConfig.consumerPrefix(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG), "earliest")
        p.put(StreamsConfig.consumerPrefix(ConsumerConfig.MAX_POLL_RECORDS_CONFIG), cfg.kafkaStreamsConfig.maxPollRecords)
        p.put(StreamsConfig.producerPrefix(ProducerConfig.BUFFER_MEMORY_CONFIG), cfg.kafkaStreamsConfig.bufferMemory)
        p.put(StreamsConfig.producerPrefix(ProducerConfig.COMPRESSION_TYPE_CONFIG), cfg.kafkaStreamsConfig.compressionType)
        p.put(StreamsConfig.producerPrefix(ProducerConfig.LINGER_MS_CONFIG), cfg.kafkaStreamsConfig.linger.toMillis)
        p
      }

      val avroSettings = AvroSettings(schemaRegistryClient)
      //  .withAutoRegisterSchemas(false)
      //  .withProperty("use.latest.version", "true")

      for {
        eventKeySerde      <- VulcanSerde[Event.Key].using(avroSettings)(true)
        parserEventSerde   <- VulcanSerde[ParserEvent].using(avroSettings)(false)
        enricherEventSerde <- VulcanSerde[EnricherEvent].using(avroSettings)(false)
        parserEventsKStream = streamsBuilder.stream(cfg.kafkaStreamsConfig.sourceTopic, Consumed.`with`(eventKeySerde, parserEventSerde))
        _                  <- Sync[F].delay {
                                parserEventsKStream
                                  .filter { (_: Event.Key, event: ParserEvent) =>
                                    event match {
                                      case _: ParserEvent.OzonCategorySearchResultsV2ItemParsed => true
                                      case _                                                    => false
                                    }
                                  }
                                  .mapValues({ (event: ParserEvent) =>
                                    val ParserEvent.OzonCategorySearchResultsV2ItemParsed(created, timestamp, page, item, category) = event
                                    EnricherEvent.OzonCategorySearchResultsV2ItemEnriched(created, timestamp, page, item, ozon.Sale.Unknown, category)
                                  }: ValueMapper[ParserEvent, EnricherEvent])
                                  .groupByKey(Grouped.`with`(eventKeySerde, enricherEventSerde))
                                  .reduce(
                                    (prevEvent: EnricherEvent, currEvent: EnricherEvent) => {
                                      val EnricherEvent.OzonCategorySearchResultsV2ItemEnriched(_, _, _, prevItem, _, _)                         = prevEvent
                                      val EnricherEvent.OzonCategorySearchResultsV2ItemEnriched(created, timestamp, page, currItem, _, category) = currEvent

                                      val sale = ozon.Sale.aggregate(List(prevItem, currItem))

                                      println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")
                                      println(s"Previous event: ${prevEvent}")
                                      println(s"Current  event: ${currEvent}")
                                      println(s"Sale: ${sale}")
                                      println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -")

                                      EnricherEvent.OzonCategorySearchResultsV2ItemEnriched(created, timestamp, page, currItem, sale, category)
                                    },
                                    Materialized.`with`(eventKeySerde, enricherEventSerde)
                                  )
                                  .toStream((key: Event.Key, event: EnricherEvent) => event.key.getOrElse(key))
                                  .to(cfg.kafkaStreamsConfig.sinkTopic, Produced.`with`[Event.Key, EnricherEvent](eventKeySerde, enricherEventSerde))
                              }
        topology            = streamsBuilder.build()
        platform           <- Platform.run[F](topology, streamsConfiguration, Duration.ofNanos(cfg.kafkaStreamsConfig.closeTimeout.toNanos)).void
      } yield platform
    }
  }

  def make[
    F[_]: Concurrent
  ](cfg: Config)(schemaRegistryClient: SchemaRegistryClient): Resource[F, Enricher[F]] =
    Resource.liftF {
      val impl = new Impl[F](cfg)(schemaRegistryClient): Enricher[F]

      impl.pure[F]
    }
}
