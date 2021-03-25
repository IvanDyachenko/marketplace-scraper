package net.dalytics.config

import scala.concurrent.duration._

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class SlidingWindowsConfig(
  maxTimeDifference: FiniteDuration,
  gracePeriod: FiniteDuration
)

object SlidingWindowsConfig {
  lazy val load: SlidingWindowsConfig = ConfigSource.default.at("sliding-windows").loadOrThrow[SlidingWindowsConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SlidingWindowsConfig] =
    ConfigSource.default.at("sliding-windows").loadF[F, SlidingWindowsConfig](blocker)
}
