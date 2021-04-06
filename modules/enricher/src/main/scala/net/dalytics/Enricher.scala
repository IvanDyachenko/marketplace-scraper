package net.dalytics

import java.time.Duration
import java.util.Properties

import tofu.syntax.monadic._
import cats.effect.{Concurrent, Resource, Sync}
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{Consumed, Grouped, Materialized, Produced, ValueMapper}
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import fs2.kafka.vulcan.AvroSettings
import compstak.kafkastreams4s.Platform

import net.dalytics.config.Config
import net.dalytics.serde.{VulcanSerde}
import net.dalytics.models.{ozon, Event}
import net.dalytics.models.parser.ParserEvent
import net.dalytics.models.enricher.EnricherEvent
import net.dalytics.clients.KafkaClient
import net.dalytics.extractors.EventTimestampExtractor

trait Enricher[F[_]] {
  def run: F[Unit]
}

object Enricher {
  def apply[F[_]](implicit ev: Enricher[F]): ev.type = ev

  private final class Impl[F[_]: Concurrent](cfg: Config)(schemaRegistryClient: SchemaRegistryClient) extends Enricher[F] {
    def run: F[Unit] = {
      val avroSettings: AvroSettings[F]    = KafkaClient.makeAvroSettings[F](schemaRegistryClient)
      val streamsBuilder: StreamsBuilder   = new StreamsBuilder
      val streamsConfiguration: Properties = KafkaClient.makeKafkaStreamsConfiguration[EventTimestampExtractor](
        cfg.kafkaConfig,
        cfg.schemaRegistryConfig,
        cfg.kafkaStreamsConfig
      )

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
                                      val _prevEvent = prevEvent.asInstanceOf[EnricherEvent.OzonCategorySearchResultsV2ItemEnriched]
                                      val _currEvent = currEvent.asInstanceOf[EnricherEvent.OzonCategorySearchResultsV2ItemEnriched]
                                      _prevEvent.aggregate(_currEvent)
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
    Resource.eval {
      val impl = new Impl[F](cfg)(schemaRegistryClient): Enricher[F]

      impl.pure[F]
    }
}
