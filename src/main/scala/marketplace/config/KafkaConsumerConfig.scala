package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaConsumerConfig(groupId: String, topic: String)

object KafkaConsumerConfig {
  lazy val load: KafkaConfig = ConfigSource.default.at("kafka-consumer").loadOrThrow[KafkaConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConfig] =
    ConfigSource.default.at("kafka-consumer").loadF[F, KafkaConfig](blocker)
}
