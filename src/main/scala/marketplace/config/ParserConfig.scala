package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class ParserConfig(
  kafkaConsumer: KafkaConsumerConfig,
  kafkaProducer: KafkaProducerConfig
)

object ParserConfig {
  lazy val load: ParserConfig = ConfigSource.default.at("parser").loadOrThrow[ParserConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ParserConfig] =
    ConfigSource.default.at("parser").loadF[F, ParserConfig](blocker)
}
