package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Lowercase
import tofu.logging.{Loggable, LoggableEnum}
import vulcan.Codec
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

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

  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable
  object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] with VulcanEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

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
    for {
      availability <- c.downField("cellTrackingInfo").get[Byte]("availability")
      decoder       = Availability.from(availability) match {
                        case Availability.PreOrder        => items.PreOrder.circeDecoder
                        case Availability.InStock         => items.InStock.circeDecoder
                        case Availability.OutOfStock      => items.OutOfStock.circeDecoder
                        case Availability.CannotBeShipped => items.CannotBeShipped.circeDecoder
                      }
      item         <- decoder.widen[Item](c)
    } yield item
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Item): FreeApplicative[Codec.Field[A, *], Item] =
    field("availability", f(_).availability).map2 {
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
        field("availableInDays", f(_).availableInDays),
        field("marketplaceSellerId", f(_).marketplaceSellerId),
        field("addToCartMinItems", f(_) match { case item: items.InStock => Some(item.addToCartMinItems); case _ => None }),
        field("addToCartMaxItems", f(_) match { case item: items.InStock => Some(item.addToCartMaxItems); case _ => None }),
        field("isAdult", f(_).isAdult),
        field("isAlcohol", f(_).isAlcohol),
        field("isAvailable", f(_).isAvailable),
        field("isSupermarket", f(_).isSupermarket),
        field("isPersonalized", f(_).isPersonalized),
        field("isPromotedProduct", f(_).isPromotedProduct),
        field("freeRest", f(_).freeRest)
      ).tupled
    } {
      // format: off
      case (
            availability,
            (
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, inDays, sellerId,
              addToCartMinItems, addToCartMaxItems,
              isAdult, isAlcohol, _, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          ) =>
        Availability.from(availability) match {
          case Availability.PreOrder          =>
            items.PreOrder(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, availability, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.InStock           =>
            items.InStock(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, availability, inDays, sellerId,
              addToCartMinItems.get, addToCartMaxItems.get,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.OutOfStock        =>
            items.OutOfStock(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, availability, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.CannotBeShipped   =>
            items.CannotBeShipped(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, availability, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
        }
      // format: on
    }
}
