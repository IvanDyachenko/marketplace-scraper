package net.dalytics.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaProducerConfig(
  topic: Map[String, String],
  compressionType: String = "none",
  maxBufferSize: Int,
  linger: FiniteDuration,
  batchSize: Int
)

object KafkaProducerConfig {
  lazy val load: KafkaProducerConfig = ConfigSource.default.at("kafka-producer").loadOrThrow[KafkaProducerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaProducerConfig] =
    ConfigSource.default.at("kafka-producer").loadF[F, KafkaProducerConfig](blocker)
}
