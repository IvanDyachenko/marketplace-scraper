package net.dalytics.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.Sync
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.{ConfigFieldMapping, ConfigReader, ConfigSource, ConvertHelpers, KebabCase, PascalCase}
import pureconfig.generic.FieldCoproductHint
import pureconfig.module.enumeratum._
import pureconfig.module.catseffect.syntax._
import supertagged.postfix._

import net.dalytics.models.{ozon, wildberries => wb}

@derive(pureconfigReader)
final case class SourcesConfig(sources: List[SourceConfig])

object SourcesConfig {
  import SourceConfig._

  lazy val load: SourcesConfig = ConfigSource.default.loadOrThrow[SourcesConfig]

  def loadF[F[_]: Sync: ContextShift]: F[SourcesConfig] =
    ConfigSource.default.loadF[F, SourcesConfig](blocker)
}

@derive(pureconfigReader)
sealed trait SourceConfig {
  def every: FiniteDuration
}

object SourceConfig {

  @derive(pureconfigReader)
  final case class WbCatalog(id: wb.Catalog.Id, every: FiniteDuration) extends SourceConfig

  @derive(pureconfigReader)
  final case class OzonSeller(pageLimit: Int, every: FiniteDuration) extends SourceConfig

  @derive(pureconfigReader)
  final case class OzonCategory(id: ozon.Category.Id, searchFilter: ozon.SearchFilter.Key, every: FiniteDuration) extends SourceConfig

  implicit val fieldCoproductHint: FieldCoproductHint[SourceConfig] = new FieldCoproductHint[SourceConfig]("type") {
    override def fieldValue(name: String) = ConfigFieldMapping(PascalCase, KebabCase)(name)
  }

  implicit val wbCatalogIdConfigReader: ConfigReader[wb.Catalog.Id] = ConfigReader.fromString[wb.Catalog.Id](
    ConvertHelpers.catchReadError(_.toLong @@ wb.Catalog.Id)
  )

  implicit val ozonCategoryIdConfigReader: ConfigReader[ozon.Category.Id] = ConfigReader.fromString[ozon.Category.Id](
    ConvertHelpers.catchReadError(_.toLong @@ ozon.Category.Id)
  )
}
