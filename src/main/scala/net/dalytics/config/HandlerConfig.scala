package net.dalytics.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class HandlerConfig(
  kafkaConsumer: KafkaConsumerConfig,
  kafkaProducer: KafkaProducerConfig
)

object HandlerConfig {
  lazy val load: HandlerConfig = ConfigSource.default.at("handler").loadOrThrow[HandlerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[HandlerConfig] =
    ConfigSource.default.at("handler").loadF[F, HandlerConfig](blocker)
}
