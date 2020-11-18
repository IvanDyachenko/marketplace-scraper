package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class HttpConfig(proxyHost: String, proxyPort: Int, maxConnections: Int, maxConnectionsPerHost: Int)

object HttpConfig {
  lazy val load: HttpConfig = ConfigSource.default.at("http").loadOrThrow[HttpConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[HttpConfig] =
    ConfigSource.default.at("http").loadF[F, HttpConfig](blocker)
}

final case class ClickhouseConfig(url: String, user: String, pass: String, threadPoolSize: Int)

object ClickhouseConfig {
  lazy val load: ClickhouseConfig = ConfigSource.default.at("clickhouse").loadOrThrow[ClickhouseConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ClickhouseConfig] =
    ConfigSource.default.at("clickhouse").loadF[F, ClickhouseConfig](blocker)
}

final case class CrawlerConfig(maxConcurrent: Int)

object CrawlerConfig {
  lazy val load: CrawlerConfig = ConfigSource.default.at("crawler").loadOrThrow[CrawlerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[CrawlerConfig] =
    ConfigSource.default.at("crawler").loadF[F, CrawlerConfig](blocker)
}
