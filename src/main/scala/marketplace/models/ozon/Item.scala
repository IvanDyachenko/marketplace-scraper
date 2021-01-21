package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
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
  categoryName: Category.Name,
  delivery: Delivery,
  availability: Int,
  availableInDays: Int,
  marketplaceSellerId: MarketplaceSeller.Id,
  addToCartQuantity: Int,
  addToCartMaxItems: Int,
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

  implicit val circeDecoder: Decoder[Item] = Decoder.instance[Item] { (c: HCursor) =>
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
      i.get[Int]("availability"),
      i.get[Int]("availableInDays"),
      i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
      c.get[Template]("templateState").map(_.addToCartQuantity.getOrElse(-1)),
      c.get[Template]("templateState").map(_.addToCartMaxItems.getOrElse(-1)),
      i.get[Boolean]("isSupermarket"),
      i.get[Boolean]("isPersonalized"),
      i.get[Boolean]("isPromotedProduct"),
      i.get[Int]("freeRest"),
      i.get[Int]("index")
    ).mapN(Item.apply)
  }

  implicit val vulcanCodec: Codec[Item] =
    Codec.record(
      name = "Item",
      namespace = "ozon.models"
    )(vulcanCodecFieldFA(_)(identity))

  def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Item): FreeApplicative[Codec.Field[A, *], Item] =
    (
      field("itemId", f.andThen(_.id)),
      field("itemType", f.andThen(_.`type`)),
      field("itemTitle", f.andThen(_.title)),
      Brand.vulcanCodecFieldFA(field)(f.andThen(_.brand)),
      Price.vulcanCodecFieldFA(field)(f.andThen(_.price)),
      Rating.vulcanCodecFieldFA(field)(f.andThen(_.rating)),
      field("categoryName", f.andThen(_.categoryName)),
      Delivery.vulcanCodecFieldFA(field)(f.andThen(_.delivery)),
      field("availability", f.andThen(_.availability)),
      field("availableInDays", f.andThen(_.availableInDays)),
      field("marketplaceSellerId", f.andThen(_.marketplaceSellerId)),
      field("addToCartQuantity", f.andThen(_.addToCartQuantity)),
      field("addToCartMaxItems", f.andThen(_.addToCartMaxItems)),
      field("isSupermarket", f.andThen(_.isSupermarket)),
      field("isPersonalized", f.andThen(_.isPersonalized)),
      field("isPromoted_product", f.andThen(_.isPromotedProduct)),
      field("freeRest", f.andThen(_.freeRest)),
      field("index", f.andThen(_.index))
    ).mapN(Item.apply)
}
