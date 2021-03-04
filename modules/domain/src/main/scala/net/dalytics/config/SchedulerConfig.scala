package net.dalytics.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class SchedulerConfig(
  kafkaProducer: KafkaProducerConfig
)

object SchedulerConfig {
  lazy val load: SchedulerConfig = ConfigSource.default.at("scheduler").loadOrThrow[SchedulerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SchedulerConfig] =
    ConfigSource.default.at("scheduler").loadF[F, SchedulerConfig](blocker)
}
