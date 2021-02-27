package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class HttpConfig(
  proxyHost: String,
  proxyPort: Int,
  requestTimeout: FiniteDuration,
  requestTimeoutForEachAttempt: FiniteDuration,
  maxTotalAttempts: Int,
  maxTotalConnections: Int,
  maxNumConnectionsPerHost: Int
//eventLoopGroupNumThreads: Int
)

object HttpConfig {
  lazy val load: HttpConfig = ConfigSource.default.at("http").loadOrThrow[HttpConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[HttpConfig] =
    ConfigSource.default.at("http").loadF[F, HttpConfig](blocker)
}
