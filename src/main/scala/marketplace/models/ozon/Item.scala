package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Lowercase
import io.circe.{Decoder, HCursor}

import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Item(
  id: Item.Id,
  category: Category.Name,
  brand: Brand,
  title: String,
  rating: Double,
  price: Price,
  `type`: Item.Type,
  marketplaceSeller: MarketplaceSeller.Id,
  availability: Int,
  availableInDays: Int,
  delivery: Delivery,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int,
  //maxItems: Int,
  countItems: Int,
  index: Int
)

object Item {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable

  object Type extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

  implicit val circeDecoder: Decoder[Item] = new Decoder[Item] {
    final def apply(c: HCursor): Decoder.Result[Item] = {
      lazy val i = c.downField("cellTrackingInfo")
      (
        i.downField("id").as[Item.Id],
        i.downField("category").as[Category.Name],
        i.as[Brand],
        i.downField("title").as[String],
        i.downField("rating").as[Double],
        i.as[Price],
        i.downField("type").as[Item.Type],
        i.downField("marketplaceSellerId").as[MarketplaceSeller.Id],
        i.downField("availability").as[Int],
        i.downField("availableInDays").as[Int],
        i.as[Delivery],
        i.downField("isSupermarket").as[Boolean],
        i.downField("isPersonalized").as[Boolean],
        i.downField("isPromotedProduct").as[Boolean],
        i.downField("freeRest").as[Int],
        i.downField("countItems").as[Int],
        i.downField("index").as[Int]
      ).mapN(Item.apply)
    }
  }
}
