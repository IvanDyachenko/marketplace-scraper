package net.dalytics.config

import scala.concurrent.duration._

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaStreamsConfig(
  applicationId: String,
  closeTimeout: FiniteDuration,
  sourceTopic: String,
  sinkTopic: String,
  numberOfStreamThreads: Int,
  maxPollRecords: Int
)

object KafkaStreamsConfig {
  lazy val load: KafkaStreamsConfig = ConfigSource.default.at("kafka-streams").loadOrThrow[KafkaStreamsConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaStreamsConfig] =
    ConfigSource.default.at("kafka-streams").loadF[F, KafkaStreamsConfig](blocker)
}
