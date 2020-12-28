package marketplace.config

import cats.syntax.apply._
import cats.effect.{Blocker, ContextShift, Sync}
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  httpConfig: HttpConfig,
  kafkaConfig: KafkaConfig,
  schemaRegistryConfig: SchemaRegistryConfig,
  schedulerConfig: SchedulerConfig,
  crawlerConfig: CrawlerConfig,
  parserConfig: ParserConfig,
  sourcesConfig: SourcesConfig
)

object Config {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[Config] =
    (
      HttpConfig.loadF[F],
      KafkaConfig.loadF[F],
      SchemaRegistryConfig.loadF[F],
      SchedulerConfig.loadF[F],
      CrawlerConfig.loadF[F],
      ParserConfig.loadF[F],
      SourcesConfig.loadF[F]
    )
      .mapN(Config.apply)
}
