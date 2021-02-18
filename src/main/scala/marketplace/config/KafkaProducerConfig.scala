package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaProducerConfig(topic: String)

object KafkaProducerConfig {
  lazy val load: KafkaConfig = ConfigSource.default.at("kafka-producer").loadOrThrow[KafkaConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConfig] =
    ConfigSource.default.at("kafka-producer").loadF[F, KafkaConfig](blocker)
}
