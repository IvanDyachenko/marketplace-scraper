package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class ClickhouseConfig(url: String, user: String, pass: String, threadPoolSize: Int)

object ClickhouseConfig {
  lazy val load: ClickhouseConfig = ConfigSource.default.at("clickhouse").loadOrThrow[ClickhouseConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ClickhouseConfig] =
    ConfigSource.default.at("clickhouse").loadF[F, ClickhouseConfig](blocker)
}
