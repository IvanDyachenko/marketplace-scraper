package net.dalytics.config

import cats.syntax.apply._
import cats.effect.Sync
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  schemaRegistryConfig: SchemaRegistryConfig,
  kafkaConfig: KafkaConfig,
  kafkaStreamsConfig: KafkaStreamsConfig,
  slidingWindowsConfig: SlidingWindowsConfig
)

object Config {
  def make[F[_]: Sync: ContextShift]: F[Config] =
    (
      SchemaRegistryConfig.loadF[F],
      KafkaConfig.loadF[F],
      KafkaStreamsConfig.loadF[F],
      SlidingWindowsConfig.loadF[F]
    ).mapN(Config.apply)
}
