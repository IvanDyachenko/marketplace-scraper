package net.dalytics.config

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
  bufferSize: Int,                 // Internal buffer size of the blaze client.
  maxTotalConnections: Int,        // Maximum connections the client will have at any specific time.
  maxTotalConnectionsPerHost: Int, // Map of [[RequestKey]] to number of max connections.
  maxWaitQueueLimit: Int,          // Maximum number of requests waiting for a connection at any specific time.
  idleTimeout: FiniteDuration,     // Duration that a connection can wait without traffic being read or written before timeout.
  connectTimeout: FiniteDuration,
  requestTimeout: FiniteDuration,  // Maximum duration from the submission of a request through reading the body before a timeout.
  requestMaxTotalAttempts: Int,
  requestMaxDelayBetweenAttempts: FiniteDuration
)

object HttpConfig {
  lazy val load: HttpConfig = ConfigSource.default.at("http").loadOrThrow[HttpConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[HttpConfig] =
    ConfigSource.default.at("http").loadF[F, HttpConfig](blocker)
}
