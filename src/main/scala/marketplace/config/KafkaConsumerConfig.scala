package marketplace.config

import scala.concurrent.duration._

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaConsumerConfig(
  groupId: String,
  topic: String,
  commitTimeout: FiniteDuration = 15 seconds,
  commitTimeWindow: FiniteDuration,
  commitEveryNOffsets: Int
)

object KafkaConsumerConfig {
  lazy val load: KafkaConsumerConfig = ConfigSource.default.at("kafka-consumer").loadOrThrow[KafkaConsumerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConsumerConfig] =
    ConfigSource.default.at("kafka-consumer").loadF[F, KafkaConsumerConfig](blocker)
}
