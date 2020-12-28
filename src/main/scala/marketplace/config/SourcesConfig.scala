package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.{ConfigFieldMapping, ConfigReader, ConfigSource, ConvertHelpers, KebabCase, PascalCase}
import pureconfig.generic.auto._
import pureconfig.generic.FieldCoproductHint
import pureconfig.module.catseffect.syntax._
import supertagged.postfix._

import marketplace.models.ozon.{Category => OzonCategory}

sealed trait SourceConfig {
  def every: FiniteDuration
}

final case class OzonCategorySourceConfig(name: OzonCategory.Name, every: FiniteDuration) extends SourceConfig

final case class SourcesConfig(sources: List[SourceConfig])

object SourcesConfig {
  lazy val load: SourcesConfig = ConfigSource.default.loadOrThrow[SourcesConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SourcesConfig] =
    ConfigSource.default.loadF[F, SourcesConfig](blocker)

  implicit val fieldCoproductHint: FieldCoproductHint[SourceConfig] = new FieldCoproductHint[SourceConfig]("type") {
    override def fieldValue(name: String) = ConfigFieldMapping(PascalCase, KebabCase)(name.dropRight("SourceConfig".length))
  }

  implicit val ozonCategoryNameConfigReader: ConfigReader[OzonCategory.Name] = ConfigReader.fromString[OzonCategory.Name](
    ConvertHelpers.catchReadError(_ @@ OzonCategory.Name)
  )

}
