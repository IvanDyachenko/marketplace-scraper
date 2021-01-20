package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Lowercase
import vulcan.Codec
import vulcan.generic.AvroNamespace
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Item(
  id: Item.Id,
  `type`: Item.Type,
  title: String,
  brand: Brand,
  price: Price,
  rating: Rating,
  category: Category.Name,
  delivery: Delivery,
//  template: Template,
  availability: Int,
  availableInDays: Int,
  marketplaceSeller: MarketplaceSeller.Id,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int,
  index: Int
)

object Item {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  @AvroNamespace("ozon.models.item")
  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable

  object Type extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] with VulcanEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

  implicit val circeDecoder: Decoder[Item] = new Decoder[Item] {
    final def apply(c: HCursor): Decoder.Result[Item] = {
      lazy val i = c.downField("cellTrackingInfo")

      (
        i.get[Item.Id]("id"),
        i.get[Item.Type]("type"),
        i.get[String]("title"),
        i.as[Brand],
        i.as[Price],
        i.as[Rating],
        i.get[Category.Name]("category"),
        i.as[Delivery],
//        c.get[Template]("templateState"),
        i.get[Int]("availability"),
        i.get[Int]("availableInDays"),
        i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
        i.get[Boolean]("isSupermarket"),
        i.get[Boolean]("isPersonalized"),
        i.get[Boolean]("isPromotedProduct"),
        i.get[Int]("freeRest"),
        i.get[Int]("index")
      ).mapN(Item.apply)
    }
  }

  implicit val vulcanCodec: Codec[Item] =
    Codec.record(
      name = "Item",
      namespace = "ozon.models"
    ) { field =>
      (
        field("item_id", _.id),
        field("item_type", _.`type`),
        field("item_title", _.title),
        (field("brand_id", _.brand.id), field("brand_name", _.brand.name)).mapN(Brand.apply),                                                           // format: off
        (field("price", _.price.price), field("price_final", _.price.finalPrice), field("price_percent_discount", _.price.discount)).mapN(Price.apply), // format: on
        (field("rating_value", _.rating.value), field("rating_count", _.rating.count)).mapN(Rating.apply),
        field("category_name", _.category),
        (field("delivery_schema", _.delivery.schema), field("delivery_time_diff_days", _.delivery.timeDiffDays)).mapN(Delivery.apply),
        field("availability", _.availability),
        field("available_in_days", _.availableInDays),
        field("marketplace_seller_id", _.marketplaceSeller),
        field("is_supermarket", _.isSupermarket),
        field("is_personalized", _.isPersonalized),
        field("is_promoted_product", _.isPromotedProduct),
        field("free_rest", _.freeRest),
        field("index", _.index)
      ).mapN(Item.apply)
    }

}
