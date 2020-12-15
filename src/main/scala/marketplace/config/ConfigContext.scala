package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.apply._
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class ConfigContext(
  httpConfig: HttpConfig,
  kafkaConfig: KafkaConfig,
  schemaRegistryConfig: SchemaRegistryConfig,
  clickhouseConfig: ClickhouseConfig,
  crawlerConfig: CrawlerConfig,
  parserConfig: ParserConfig
)

object ConfigContext {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ConfigContext] =
    (
      HttpConfig.loadF[F],
      KafkaConfig.loadF[F],
      SchemaRegistryConfig.loadF[F],
      ClickhouseConfig.loadF[F],
      CrawlerConfig.loadF[F],
      ParserConfig.loadF[F]
    )
      .mapN(ConfigContext.apply)
}
