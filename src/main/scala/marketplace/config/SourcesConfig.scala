package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import pureconfig.{ConfigFieldMapping, ConfigReader, ConfigSource, ConvertHelpers, KebabCase, PascalCase}
import pureconfig.generic.auto._
import pureconfig.generic.FieldCoproductHint
import pureconfig.module.enumeratum._
import pureconfig.module.catseffect.syntax._
import supertagged.postfix._
import enumeratum.{Enum, EnumEntry}
import enumeratum.EnumEntry.Snakecase

import marketplace.models.ozon

sealed trait SourceConfig {
  def every: FiniteDuration
}

object SourceConfig {
  sealed trait Pages extends EnumEntry with Snakecase

  object Pages extends Enum[Pages] {
    case object Top extends Pages
    case object All extends Pages

    val values = findValues
  }

  final case class OzonCategory(name: ozon.Category.Name, pages: Pages, every: FiniteDuration) extends SourceConfig

  implicit val fieldCoproductHint: FieldCoproductHint[SourceConfig] = new FieldCoproductHint[SourceConfig]("type") {
    override def fieldValue(name: String) = ConfigFieldMapping(PascalCase, KebabCase)(name)
  }

  implicit val ozonCategoryNameConfigReader: ConfigReader[ozon.Category.Name] = ConfigReader.fromString[ozon.Category.Name](
    ConvertHelpers.catchReadError(_ @@ ozon.Category.Name)
  )
}

final case class SourcesConfig(sources: List[SourceConfig])

object SourcesConfig {
  import SourceConfig._

  lazy val load: SourcesConfig = ConfigSource.default.loadOrThrow[SourcesConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SourcesConfig] =
    ConfigSource.default.loadF[F, SourcesConfig](blocker)
}
