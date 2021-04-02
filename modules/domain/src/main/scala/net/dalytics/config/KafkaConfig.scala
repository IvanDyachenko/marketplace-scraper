package net.dalytics.config

import cats.effect.Sync
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaConfig(bootstrapServers: String)

object KafkaConfig {
  lazy val load: KafkaConfig = ConfigSource.default.at("kafka").loadOrThrow[KafkaConfig]

  def loadF[F[_]: Sync: ContextShift]: F[KafkaConfig] =
    ConfigSource.default.at("kafka").loadF[F, KafkaConfig](blocker)
}
