package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.{Camelcase, Uppercase}
import io.circe.Decoder
import vulcan.AvroNamespace

@derive(loggable)
final case class Delivery(schema: Delivery.Schema, timeDiffDays: Int)

object Delivery {

  @AvroNamespace("ozon.models.delivery")
  sealed trait Schema extends EnumEntry with Camelcase with Product with Serializable

  object Schema extends Enum[Schema] with CatsEnum[Schema] with CirceEnum[Schema] with LoggableEnum[Schema] with VulcanEnum[Schema] {

    case object FBO         extends Schema with Uppercase // Товар продается со склада Ozon.
    case object FBS         extends Schema with Uppercase // Товар продается со склада продавца.
    case object Retail      extends Schema                // Трансграничная торговля.
    case object Crossborder extends Schema                // Трансграничная торговля.

    val values = findValues
  }

  implicit val circeDecoder: Decoder[Delivery] = Decoder.forProduct2("deliverySchema", "deliveryTimeDiffDays")(apply)
}
