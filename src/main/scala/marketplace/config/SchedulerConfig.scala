package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class SchedulerConfig(
  maxConcurrent: Int
)

object SchedulerConfig {
  lazy val load: SchedulerConfig = ConfigSource.default.at("scheduler").loadOrThrow[SchedulerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SchedulerConfig] =
    ConfigSource.default.at("scheduler").loadF[F, SchedulerConfig](blocker)
}
