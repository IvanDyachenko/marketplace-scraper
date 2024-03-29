package net.dalytics.config

import cats.syntax.apply._
import cats.effect.{Blocker, ContextShift, Sync}
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  httpConfig: HttpConfig,
  schemaRegistryConfig: SchemaRegistryConfig,
  kafkaConfig: KafkaConfig,
  kafkaProducerConfig: KafkaProducerConfig,
  tasksConfig: TasksConfig,
  apiRateLimitsConfig: ApiRateLimitsConfig
)

object Config {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[Config] =
    (
      HttpConfig.loadF[F],
      SchemaRegistryConfig.loadF[F],
      KafkaConfig.loadF[F],
      KafkaProducerConfig.loadF[F],
      TasksConfig.loadF[F],
      ApiRateLimitsConfig.loadF[F]
    )
      .mapN(Config.apply)
}
