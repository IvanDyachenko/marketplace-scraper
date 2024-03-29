package net.dalytics.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaConfig(
  bootstrapServers: String // A list of host/port pairs to use for establishing the initial connection to the Kafka cluster.
)

object KafkaConfig {
  lazy val load: KafkaConfig = ConfigSource.default.at("kafka").loadOrThrow[KafkaConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConfig] =
    ConfigSource.default.at("kafka").loadF[F, KafkaConfig](blocker)
}
