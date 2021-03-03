package net.dalytics.config

import cats.syntax.apply._
import cats.effect.{Blocker, ContextShift, Sync}
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  httpConfig: HttpConfig,
  kafkaConfig: KafkaConfig,
  schemaRegistryConfig: SchemaRegistryConfig,
  sourcesConfig: SourcesConfig,
  parserConfig: ParserConfig,
  crawlerConfig: CrawlerConfig,
  schedulerConfig: SchedulerConfig
)

object Config {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[Config] =
    (
      HttpConfig.loadF[F],
      KafkaConfig.loadF[F],
      SchemaRegistryConfig.loadF[F],
      SourcesConfig.loadF[F],
      ParserConfig.loadF[F],
      CrawlerConfig.loadF[F],
      SchedulerConfig.loadF[F]
    )
      .mapN(Config.apply)
}
