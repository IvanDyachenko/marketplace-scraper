package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class CrawlerConfig(
  groupId: String,
  eventsTopic: String,
  commandsTopic: String,
  batchOffsets: Int,
  batchTimeWindow: FiniteDuration
)

object CrawlerConfig {
  lazy val load: CrawlerConfig = ConfigSource.default.at("crawler").loadOrThrow[CrawlerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[CrawlerConfig] =
    ConfigSource.default.at("crawler").loadF[F, CrawlerConfig](blocker)
}
