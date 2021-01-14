package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Uppercase
import tofu.logging.LoggableEnum

@derive(loggable)
final case class Delivery(schema: Delivery.Schema, timeDiffDays: Int)

object Delivery {

  sealed trait Schema extends EnumEntry with Uppercase with Product with Serializable

  object Schema extends Enum[Schema] with CatsEnum[Schema] with CirceEnum[Schema] with LoggableEnum[Schema] {

    case object FBO         extends Schema // Товар продается со склада Ozon.
    case object FBS         extends Schema // Товар продается со склада продавца.
    case object Crossborder extends Schema // Трансграничная торговля.

    val values = findValues
  }

  implicit val circeDecoder: Decoder[Delivery] = Decoder.forProduct2("deliverySchema", "deliveryTimeDiffDays")(apply)
}
