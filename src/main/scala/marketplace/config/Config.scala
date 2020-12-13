package marketplace.config

import scala.concurrent.duration.FiniteDuration

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

final case class SchemaRegistryConfig(baseUrl: String)

object SchemaRegistryConfig {
  lazy val load: SchemaRegistryConfig = ConfigSource.default.at("schema-registry").loadOrThrow[SchemaRegistryConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SchemaRegistryConfig] =
    ConfigSource.default.at("schema-registry").loadF[F, SchemaRegistryConfig](blocker)
}

final case class ClickhouseConfig(url: String, user: String, pass: String, threadPoolSize: Int)

object ClickhouseConfig {
  lazy val load: ClickhouseConfig = ConfigSource.default.at("clickhouse").loadOrThrow[ClickhouseConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ClickhouseConfig] =
    ConfigSource.default.at("clickhouse").loadF[F, ClickhouseConfig](blocker)
}

final case class CrawlerConfig(commandsTopic: String, batchOffsets: Int, batchTimeWindow: FiniteDuration)

object CrawlerConfig {
  lazy val load: CrawlerConfig = ConfigSource.default.at("crawler").loadOrThrow[CrawlerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[CrawlerConfig] =
    ConfigSource.default.at("crawler").loadF[F, CrawlerConfig](blocker)
}

final case class ParserConfig(maxOpen: Int, maxConcurrent: Int)

object ParserConfig {
  lazy val load: ParserConfig = ConfigSource.default.at("parser").loadOrThrow[ParserConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ParserConfig] =
    ConfigSource.default.at("parser").loadF[F, ParserConfig](blocker)
}
