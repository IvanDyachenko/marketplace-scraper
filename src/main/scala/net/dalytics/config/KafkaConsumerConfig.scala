package net.dalytics.config

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
  maxPollRecords: Option[Int],
  maxPollInterval: Option[FiniteDuration],
  commitTimeout: Option[FiniteDuration],
  commitTimeWindow: FiniteDuration,
  commitEveryNOffsets: Int,
  maxConcurrentPerPartition: Int
)

object KafkaConsumerConfig {
  lazy val load: KafkaConsumerConfig = ConfigSource.default.at("kafka-consumer").loadOrThrow[KafkaConsumerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConsumerConfig] =
    ConfigSource.default.at("kafka-consumer").loadF[F, KafkaConsumerConfig](blocker)
}
