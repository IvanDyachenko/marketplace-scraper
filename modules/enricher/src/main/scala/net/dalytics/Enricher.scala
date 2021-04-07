package net.dalytics

import java.time.Duration
import java.util.Properties

import tofu.syntax.monadic._
import cats.effect.{Concurrent, Resource, Sync}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import fs2.kafka.vulcan.AvroSettings
import compstak.kafkastreams4s.Platform

import net.dalytics.config.Config
import net.dalytics.models.Event
import net.dalytics.models.parser.ParserEvent
import net.dalytics.models.enricher.EnricherEvent
import net.dalytics.clients.KafkaClient
import net.dalytics.wrappers.vulcan.KStream4sVulcan
import net.dalytics.serdes.VulcanSerde
import net.dalytics.extractors.EventTimestampExtractor

trait Enricher[F[_]] {
  def run: F[Unit]
}

object Enricher {
  def apply[F[_]](implicit ev: Enricher[F]): ev.type = ev

  private final class Impl[F[_]: Concurrent](cfg: Config)(schemaRegistryClient: SchemaRegistryClient) extends Enricher[F] {
    def run: F[Unit] = {
      val avroSettings: AvroSettings[F]  = KafkaClient.makeAvroSettings[F](schemaRegistryClient)
      val streamsConfig: Properties      = KafkaClient.makeKafkaStreamsConfiguration[EventTimestampExtractor](
        cfg.kafkaConfig,
        cfg.schemaRegistryConfig,
        cfg.kafkaStreamsConfig
      )
      val streamsTimeout: Duration       = Duration.ofNanos(cfg.kafkaStreamsConfig.closeTimeout.toNanos)
      val streamsBuilder: StreamsBuilder = new StreamsBuilder

      for {
        implicit0(eventKeySerde: Serde[Event.Key])                                            <- VulcanSerde[Event.Key].using(avroSettings)(true)
        implicit0(parserEventSerde: Serde[ParserEvent])                                       <- VulcanSerde[ParserEvent].using(avroSettings)(false)
        implicit0(enricherEventSerde: Serde[EnricherEvent.OzonCategoryResultsV2ItemEnriched]) <-
          VulcanSerde[EnricherEvent.OzonCategoryResultsV2ItemEnriched].using(avroSettings)(false)

        _ <- Sync[F].delay {
               KStream4sVulcan[Event.Key, ParserEvent](streamsBuilder, cfg.kafkaStreamsConfig.sourceTopics)
                 .collect {
                   case ParserEvent.OzonCategorySearchResultsV2ItemParsed(created, timestamp, page, item, category)  =>
                     EnricherEvent.OzonCategoryResultsV2ItemEnriched(created, timestamp, page, item, category)
                   case ParserEvent.OzonCategorySoldOutResultsV2ItemParsed(created, timestamp, page, item, category) =>
                     EnricherEvent.OzonCategoryResultsV2ItemEnriched(created, timestamp, page, item, category)
                 }
                 .reduceByKey(_ aggregate _)
                 .toKTable
                 .toStream((_, event) => event.key.get)
                 .to(
                   cfg.kafkaStreamsConfig.sinkTopic,
                   Produced.`with`[Event.Key, EnricherEvent.OzonCategoryResultsV2ItemEnriched](eventKeySerde, enricherEventSerde)
                 )
             }

        topology  = streamsBuilder.build()
        platform <- Platform.run[F](topology, streamsConfig, streamsTimeout).void
      } yield platform
    }
  }

  def make[F[_]: Concurrent](cfg: Config)(schemaRegistryClient: SchemaRegistryClient): Resource[F, Enricher[F]] =
    Resource.eval {
      val impl = new Impl[F](cfg)(schemaRegistryClient): Enricher[F]

      impl.pure[F]
    }
}
