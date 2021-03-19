package net.dalytics.config

import cats.syntax.apply._
import cats.effect.{Blocker, ContextShift, Sync}
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  schemaRegistryConfig: SchemaRegistryConfig,
  kafkaConfig: KafkaConfig,
  kafkaStreamsConfig: KafkaStreamsConfig
)

object Config {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[Config] =
    (
      SchemaRegistryConfig.loadF[F],
      KafkaConfig.loadF[F],
      KafkaStreamsConfig.loadF[F]
    ).mapN(Config.apply)
}
