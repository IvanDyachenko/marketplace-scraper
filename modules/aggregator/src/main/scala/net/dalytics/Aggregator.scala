package net.dalytics

import java.time.Duration
import java.util.Properties

import tofu.syntax.monadic._
import cats.effect.{Concurrent, Resource, Sync}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{StreamsBuilder, StreamsConfig}
//import org.apache.kafka.streams.kstream.{Consumed, Produced}
import compstak.kafkastreams4s.Platform
import compstak.kafkastreams4s.vulcan.VulcanTable
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig

import net.dalytics.config.Config
import net.dalytics.models.Command
import net.dalytics.models.handler.HandlerCommand
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.Produced

trait Aggregator[F[_]] {
  def run: F[compstak.kafkastreams4s.ShutdownStatus]
}

object Aggregator {
  def apply[F[_]](implicit ev: Aggregator[F]): ev.type = ev

  private final class Impl[F[_]: Concurrent](cfg: Config)(
    builder: StreamsBuilder,
    table: VulcanTable[Command.Key, HandlerCommand],
    keySerde: Serde[Command.Key],
    valueSerde: Serde[HandlerCommand]
  ) extends Aggregator[F] {
    def run: F[compstak.kafkastreams4s.ShutdownStatus] = {
      val outputTopic = "output4"

      val streamsConfiguration: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, outputTopic)
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, cfg.kafkaConfig.bootstrapServers)
        p.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, cfg.schemaRegistryConfig.baseUrl)
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
      }

      val topology =
        Sync[F].delay {
          table
            .toKTable
            .toStream()
            .to(
              outputTopic,
              Produced.`with`(keySerde, valueSerde)
            )
        } >> builder.build().pure[F]

      topology.flatMap(topo => Platform.run[F](topo, streamsConfiguration, Duration.ofSeconds(3)))
    }
  }

  def make[
    F[_]: Concurrent
  ](cfg: Config)(
    builder: StreamsBuilder,
    table: VulcanTable[Command.Key, HandlerCommand],
    keySerde: Serde[Command.Key],
    valueSerde: Serde[HandlerCommand]
  ): Resource[F, Aggregator[F]] =
    Resource.liftF {
      val impl = new Impl[F](cfg)(builder, table, keySerde, valueSerde): Aggregator[F]

      impl.pure[F]
    }
}
