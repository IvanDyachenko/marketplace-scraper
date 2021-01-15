package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Lowercase
import io.circe.{Decoder, HCursor, Json}

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
  template: List[Template.State],
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
        i.get[Item.Id]("id"),
        i.get[Category.Name]("category"),
        i.as[Brand],
        i.get[String]("title"),
        i.get[Double]("rating"),
        i.as[Price],
        i.get[Item.Type]("type"),
        i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
        i.get[Int]("availability"),
        i.get[Int]("availableInDays"),
        i.as[Delivery],
        i.get[Boolean]("isSupermarket"),
        i.get[Boolean]("isPersonalized"),
        i.get[Boolean]("isPromotedProduct"),
        i.get[Int]("freeRest"),
        c.get[List[Template.State]]("templateState")(decodeListTolerantly[Template.State]),
        i.get[Int]("countItems"),
        i.get[Int]("index")
      ).mapN(Item.apply)
    }
  }

  private def decodeListTolerantly[A: Decoder]: Decoder[List[A]] =
    Decoder
      .decodeList(Decoder[A].either(Decoder[Json]))
      .map(_.flatMap(_.left.toOption))
}
