package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class KafkaConfig(bootstrapServers: String)

object KafkaConfig {
  lazy val load: KafkaConfig = ConfigSource.default.at("kafka").loadOrThrow[KafkaConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConfig] =
    ConfigSource.default.at("kafka").loadF[F, KafkaConfig](blocker)
}
