package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

final case class SchemaRegistryConfig(baseUrl: String)

object SchemaRegistryConfig {
  lazy val load: SchemaRegistryConfig = ConfigSource.default.at("schema-registry").loadOrThrow[SchemaRegistryConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SchemaRegistryConfig] =
    ConfigSource.default.at("schema-registry").loadF[F, SchemaRegistryConfig](blocker)
}
