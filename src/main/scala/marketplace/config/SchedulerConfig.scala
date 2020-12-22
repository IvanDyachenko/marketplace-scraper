package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class SchedulerConfig(
  timeout: FiniteDuration
)

object SchedulerConfig {
  lazy val load: SchedulerConfig = ConfigSource.default.at("scheduler").loadOrThrow[SchedulerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SchedulerConfig] =
    ConfigSource.default.at("scheduler").loadF[F, SchedulerConfig](blocker)
}
