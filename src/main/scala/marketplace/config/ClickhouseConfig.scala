package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class ClickhouseConfig(url: String, user: String, pass: String, threadPoolSize: Int)

object ClickhouseConfig {
  lazy val load: ClickhouseConfig = ConfigSource.default.at("clickhouse").loadOrThrow[ClickhouseConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ClickhouseConfig] =
    ConfigSource.default.at("clickhouse").loadF[F, ClickhouseConfig](blocker)
}
