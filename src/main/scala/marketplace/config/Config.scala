package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.apply._
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  httpConfig: HttpConfig,
  kafkaConfig: KafkaConfig,
  schemaRegistryConfig: SchemaRegistryConfig,
  crawlerConfig: CrawlerConfig,
  parserConfig: ParserConfig
)

object Config {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[Config] =
    (HttpConfig.loadF[F], KafkaConfig.loadF[F], SchemaRegistryConfig.loadF[F], CrawlerConfig.loadF[F], ParserConfig.loadF[F])
      .mapN(Config.apply)
}
