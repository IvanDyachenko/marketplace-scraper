package marketplace.config

import scala.concurrent.duration.FiniteDuration

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.{ConfigFieldMapping, ConfigReader, ConfigSource, ConvertHelpers, KebabCase, PascalCase}
import pureconfig.generic.FieldCoproductHint
import pureconfig.module.enumeratum._
import pureconfig.module.catseffect.syntax._
import supertagged.postfix._

import marketplace.models.ozon

@derive(pureconfigReader)
final case class SourcesConfig(sources: List[SourceConfig])

object SourcesConfig {
  import SourceConfig._

  lazy val load: SourcesConfig = ConfigSource.default.loadOrThrow[SourcesConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[SourcesConfig] =
    ConfigSource.default.loadF[F, SourcesConfig](blocker)
}

@derive(pureconfigReader)
sealed trait SourceConfig {
  def every: FiniteDuration
}

object SourceConfig {
  @derive(pureconfigReader)
  final case class OzonCategory(id: ozon.Category.Id, every: FiniteDuration) extends SourceConfig

  implicit val fieldCoproductHint: FieldCoproductHint[SourceConfig] = new FieldCoproductHint[SourceConfig]("type") {
    override def fieldValue(name: String) = ConfigFieldMapping(PascalCase, KebabCase)(name)
  }

  implicit val ozonCategoryIdConfigReader: ConfigReader[ozon.Category.Id] = ConfigReader.fromString[ozon.Category.Id](
    ConvertHelpers.catchReadError(_.toLong @@ ozon.Category.Id)
  )
}
