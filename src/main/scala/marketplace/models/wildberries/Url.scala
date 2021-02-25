package marketplace.models.wildberries

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase
import tofu.logging.LoggableEnum

@derive(loggable)
final case class Url(`type`: Url.Type, path: String, query: Option[String])

object Url {

  sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
  object Type       extends Enum[Type] with CatsEnum[Type] with LoggableEnum[Type] with CirceEnum[Type] {
    val values = findValues

    case object Catalog   extends Type
    case object Catalog2  extends Type
    case object Xfilter   extends Type
    case object External  extends Type
    case object BrandList extends Type
  }

  implicit val circeDecoder: Decoder[Url] = Decoder.forProduct3("urlType", "pageUrl", "query")(apply)
}
