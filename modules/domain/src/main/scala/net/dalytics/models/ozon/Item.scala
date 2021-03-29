package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import tofu.logging.{Loggable, LoggableEnum}
import vulcan.Codec
import vulcan.generic.AvroNamespace
import io.circe.{Decoder, HCursor}
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Lowercase
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

trait Item {
  def id: Item.Id
  def index: Int
  def `type`: Item.Type
  def title: String
  def brand: Brand
  def price: Price
  def rating: Rating
  def categoryPath: Category.Path
  def delivery: Delivery
  def availability: Short
  def availableInDays: Short
  def marketplaceSellerId: MarketplaceSeller.Id
  def addToCart: AddToCart
  def isAdult: Boolean
  def isAlcohol: Boolean
  def isAvailable: Boolean = Item.Availability.from(availability) == Item.Availability.InStock
  def isSupermarket: Boolean
  def isPersonalized: Boolean
  def isPromotedProduct: Boolean
  def freeRest: Int
}

object Item {

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Id = Id.Type

  @AvroNamespace("ozon.models.item")
  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable
  object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] with VulcanEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

  @AvroNamespace("ozon.models.item")
  sealed abstract class Availability extends EnumEntry
  object Availability                extends Enum[Availability] with CirceEnum[Availability] with VulcanEnum[Availability] {
    val values = findValues

    case object InStock         extends Availability
    case object OutOfStock      extends Availability
    case object PreOrder        extends Availability
    case object CannotBeShipped extends Availability

    def from: Short => Availability =
      _ match {
        case 1     => InStock
        case 0 | 2 => OutOfStock
        case 3     => PreOrder
        case 5 | 6 => CannotBeShipped
      }
  }

  implicit val loggable: Loggable[Item] = Loggable.empty

  implicit val circeDecoder: Decoder[Item] = Decoder.instance[Item] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      availability <- i.get[Short]("availability").map(Availability.from)
      decoder       = availability match {
                        case Availability.PreOrder        => PreOrder.circeDecoder
                        case Availability.InStock         => InStock.circeDecoder
                        case Availability.OutOfStock      => OutOfStock.circeDecoder
                        case Availability.CannotBeShipped => CannotBeShipped.circeDecoder
                      }
      item         <- decoder.widen[Item](c)
    } yield item
  }

  // Fix me:( It looks terrible!
  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Item): FreeApplicative[Codec.Field[A, *], Item] =
    (
      field("itemId", f(_).id),
      field("itemIndex", f(_).index),
      field("itemType", f(_).`type`),
      field("itemTitle", f(_).title),
      Brand.vulcanCodecFieldFA(field)(f(_).brand),
      Price.vulcanCodecFieldFA(field)(f(_).price),
      Rating.vulcanCodecFieldFA(field)(f(_).rating),
      field("categoryPath", f(_).categoryPath),
      Delivery.vulcanCodecFieldFA(field)(f(_).delivery),
      field("availability", f(_).availability),
      field("availableInDays", f(_).availableInDays),
      field("marketplaceSellerId", f(_).marketplaceSellerId),
      AddToCart.vulcanCodecFieldFA(field)(f(_).addToCart),
      field("isAdult", f(_).isAdult),
      field("isAlcohol", f(_).isAlcohol),
      field("isAvailable", f(_).isAvailable),
      field("isSupermarket", f(_).isSupermarket),
      field("isPersonalized", f(_).isPersonalized),
      field("isPromotedProduct", f(_).isPromotedProduct),
      field("freeRest", f(_).freeRest)
    ).mapN {
      case (
            itemId,
            itemIndex,
            itemType,
            itemTitle,
            brand,
            price,
            rating,
            categoryPath,
            delivery,
            availability,
            availableInDays,
            marketplaceSellerId,
            addToCart,
            isAdult,
            isAlcohol,
            _,
            isSupermarket,
            isPersonalized,
            isPromotedProduct,
            freeRest
          ) =>
        Availability.from(availability) match {
          case Availability.PreOrder        =>
            PreOrder(
              itemId,
              itemIndex,
              itemType,
              itemTitle,
              brand,
              price,
              rating,
              categoryPath,
              delivery,
              availability,
              availableInDays,
              marketplaceSellerId,
              isAdult,
              isAlcohol,
              isSupermarket,
              isPersonalized,
              isPromotedProduct,
              freeRest
            )
          case Availability.InStock         =>
            InStock(
              itemId,
              itemIndex,
              itemType,
              itemTitle,
              brand,
              price,
              rating,
              categoryPath,
              delivery,
              availability,
              availableInDays,
              marketplaceSellerId,
              addToCart,
              isAdult,
              isAlcohol,
              isSupermarket,
              isPersonalized,
              isPromotedProduct,
              freeRest
            )
          case Availability.OutOfStock      =>
            OutOfStock(
              itemId,
              itemIndex,
              itemType,
              itemTitle,
              brand,
              price,
              rating,
              categoryPath,
              delivery,
              availability,
              availableInDays,
              marketplaceSellerId,
              isAdult,
              isAlcohol,
              isSupermarket,
              isPersonalized,
              isPromotedProduct,
              freeRest
            )
          case Availability.CannotBeShipped =>
            CannotBeShipped(
              itemId,
              itemIndex,
              itemType,
              itemTitle,
              brand,
              price,
              rating,
              categoryPath,
              delivery,
              availability,
              availableInDays,
              marketplaceSellerId,
              isAdult,
              isAlcohol,
              isSupermarket,
              isPersonalized,
              isPromotedProduct,
              freeRest
            )
        }
    }
}
