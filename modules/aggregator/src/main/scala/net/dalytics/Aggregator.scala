package net.dalytics

import java.time.Duration
import java.util.{Collections, Properties}

import tofu.syntax.monadic._
import cats.effect.{Concurrent, Resource, Sync}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig}
import org.apache.kafka.streams.kstream.{Consumed, Produced}
import compstak.kafkastreams4s.Platform
import compstak.kafkastreams4s.vulcan.{VulcanSerdes, VulcanTable}
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig

import net.dalytics.config.Config
import net.dalytics.models.Command
import net.dalytics.models.handler.HandlerCommand

trait Aggregator[F[_]] {
  def run: F[compstak.kafkastreams4s.ShutdownStatus]
}

object Aggregator {
  def apply[F[_]](implicit ev: Aggregator[F]): ev.type = ev

  private final class Impl[F[_]: Concurrent](cfg: Config) extends Aggregator[F] {
    def run: F[compstak.kafkastreams4s.ShutdownStatus] = {
      // TODO: REMOVE. FOR TESTING PURPOSES ONLY
      val inputTopic  = "marketplace_handler-commands-handle_ozon_request-version_1"
      val outputTopic = "output"

      val builder = new StreamsBuilder

      val streamsConfiguration: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, outputTopic)
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.kafkaConfig.bootstrapServers)
        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, cfg.schemaRegistryConfig.baseUrl)
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
      }

      val serdeConfig = Collections.singletonMap(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        cfg.schemaRegistryConfig.baseUrl
      )

      implicit val keyAvroSerde: Serde[Command.Key] = {
        val serde = VulcanSerdes.serdeForVulcan[Command.Key]
        serde.configure(serdeConfig, true)
        serde
      }

      implicit val valueAvroSerde: Serde[HandlerCommand.HandleOzonRequest] = {
        val serde = VulcanSerdes.serdeForVulcan[HandlerCommand.HandleOzonRequest]
        serde.configure(serdeConfig, false)
        serde
      }

      val table = VulcanTable.fromKTable(builder.table(inputTopic, Consumed.`with`(keyAvroSerde, valueAvroSerde)))

      val topology =
        Sync[F].delay {
          table
            .map(cmd => cmd)
            .toKTable
            .toStream()
            .to(
              outputTopic,
              Produced.`with`(keyAvroSerde, valueAvroSerde)
            )
        } >> builder.build().pure[F]

      topology.flatMap(topo => Platform.run[F](topo, streamsConfiguration, Duration.ofSeconds(3)))
    }
    // TODO: REMOVE. FOR TESTING PURPOSES ONLY
  }

  def make[
    F[_]: Concurrent
  ](cfg: Config): Resource[F, Aggregator[F]] =
    Resource.liftF {
      val impl = new Impl[F](cfg): Aggregator[F]

      impl.pure[F]
    }
}
