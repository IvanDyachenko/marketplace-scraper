package net.dalytics.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class CrawlerConfig(
  kafkaConsumer: KafkaConsumerConfig,
  kafkaProducer: KafkaProducerConfig
)

object CrawlerConfig {
  lazy val load: CrawlerConfig = ConfigSource.default.at("crawler").loadOrThrow[CrawlerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[CrawlerConfig] =
    ConfigSource.default.at("crawler").loadF[F, CrawlerConfig](blocker)
}
