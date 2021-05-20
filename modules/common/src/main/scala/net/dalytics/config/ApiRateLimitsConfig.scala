package net.dalytics.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class ApiRateLimitsConfig(ozon: OzonApiRateLimitsConfig)

@derive(pureconfigReader)
final case class OzonApiRateLimitsConfig(
  searchFilters: Int,
  searchPage: Int,
  soldOutPage: Int
)

object ApiRateLimitsConfig {
  lazy val load: ApiRateLimitsConfig = ConfigSource.default.at("api-rate-limits").loadOrThrow[ApiRateLimitsConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ApiRateLimitsConfig] =
    ConfigSource.default.at("api-rate-limits").loadF[F, ApiRateLimitsConfig](blocker)
}
