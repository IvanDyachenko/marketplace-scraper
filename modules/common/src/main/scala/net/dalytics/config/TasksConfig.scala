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
final case class TasksConfig(tasks: List[TaskConfig])

object TasksConfig {
  import TaskConfig._

  lazy val load: TasksConfig = ConfigSource.default.loadOrThrow[TasksConfig]

  def loadF[F[_]: Sync: ContextShift]: F[TasksConfig] =
    ConfigSource.default.loadF[F, TasksConfig](blocker)
}

@derive(pureconfigReader)
sealed trait TaskConfig {
  def every: FiniteDuration
}

object TaskConfig {

  @derive(pureconfigReader)
  final case class WbCatalog(id: wb.Catalog.Id, every: FiniteDuration) extends TaskConfig

  @derive(pureconfigReader)
  final case class OzonSeller(pageLimit: Int, every: FiniteDuration) extends TaskConfig

  @derive(pureconfigReader)
  final case class OzonCategory(id: ozon.Category.Id, splitBy: ozon.SearchFilter.Key, every: FiniteDuration) extends TaskConfig

  implicit val fieldCoproductHint: FieldCoproductHint[TaskConfig] = new FieldCoproductHint[TaskConfig]("type") {
    override def fieldValue(name: String) = ConfigFieldMapping(PascalCase, KebabCase)(name)
  }

  implicit val wbCatalogIdConfigReader: ConfigReader[wb.Catalog.Id] = ConfigReader.fromString[wb.Catalog.Id](
    ConvertHelpers.catchReadError(_.toLong @@ wb.Catalog.Id)
  )

  implicit val ozonCategoryIdConfigReader: ConfigReader[ozon.Category.Id] = ConfigReader.fromString[ozon.Category.Id](
    ConvertHelpers.catchReadError(_.toLong @@ ozon.Category.Id)
  )
}
