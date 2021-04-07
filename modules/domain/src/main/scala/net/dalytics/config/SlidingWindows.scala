package net.dalytics.config

import scala.concurrent.duration._

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class SlidingWindowsConfig(
  maxTimeDifference: FiniteDuration, // The max time difference (inclusive) between two records in a window.
  gracePeriod: FiniteDuration        // The grace period to admit out-of-order events to a window.
)

object SlidingWindowsConfig {
  lazy val load: SlidingWindowsConfig = ConfigSource.default.at("sliding-windows").loadOrThrow[SlidingWindowsConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SlidingWindowsConfig] =
    ConfigSource.default.at("sliding-windows").loadF[F, SlidingWindowsConfig](blocker)
}
