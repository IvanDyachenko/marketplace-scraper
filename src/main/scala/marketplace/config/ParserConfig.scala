package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class ParserConfig(
  kafkaConsumerConfig: KafkaConsumerConfig,
  kafkaProducerConfig: KafkaProducerConfig,
  maxConcurrent: Int,
  batchOffsets: Int,
  batchTimeWindow: FiniteDuration
)

object ParserConfig {
  lazy val load: ParserConfig = ConfigSource.default.at("parser").loadOrThrow[ParserConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ParserConfig] =
    ConfigSource.default.at("parser").loadF[F, ParserConfig](blocker)
}
