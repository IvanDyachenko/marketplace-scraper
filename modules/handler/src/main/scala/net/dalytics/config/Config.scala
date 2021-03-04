package net.dalytics.config

import cats.syntax.apply._
import cats.effect.{Blocker, ContextShift, Sync}
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class Config(
  httpConfig: HttpConfig,
  schemaRegistryConfig: SchemaRegistryConfig,
  kafkaConfig: KafkaConfig,
  kafkaConsumerConfig: KafkaConsumerConfig,
  kafkaProducerConfig: KafkaProducerConfig
)

object Config {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[Config] =
    (HttpConfig.loadF[F], SchemaRegistryConfig.loadF[F], KafkaConfig.loadF[F], KafkaConsumerConfig.loadF[F], KafkaProducerConfig.loadF[F])
      .mapN(Config.apply)
}
