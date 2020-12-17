package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class ParserConfig(maxOpen: Int, maxConcurrent: Int)

object ParserConfig {
  lazy val load: ParserConfig = ConfigSource.default.at("parser").loadOrThrow[ParserConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ParserConfig] =
    ConfigSource.default.at("parser").loadF[F, ParserConfig](blocker)
}
