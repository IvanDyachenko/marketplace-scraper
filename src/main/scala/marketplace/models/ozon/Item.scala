package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Lowercase
import enumeratum.values.{ByteCirceEnum, ByteEnum, ByteEnumEntry, ByteVulcanEnum}
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
  def availability: Item.Availability
  def availableInDays: Short
  def marketplaceSellerId: MarketplaceSeller.Id
  def isAdult: Boolean
  def isAlcohol: Boolean
  def isAvailable: Boolean = availability == Item.Availability.InStock
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

  sealed abstract class Availability(val value: Byte) extends ByteEnumEntry
  object Availability                                 extends ByteEnum[Availability] with ByteCirceEnum[Availability] with ByteVulcanEnum[Availability] {
    val values = findValues

    case object OutOfStock        extends Availability(0)
    case object InStock           extends Availability(1)
    case object OutOfStockAnalogs extends Availability(2)
    case object PreOrder          extends Availability(3)
    case object CannotBeShipped   extends Availability(6)
  }

  implicit val loggable: Loggable[Item] = Loggable.empty

  implicit val circeDecoder: Decoder[Item] = Decoder.instance[Item] { (c: HCursor) =>
    for {
      availability <- c.downField("cellTrackingInfo").get[Availability]("availability")
      decoder       = availability match {
                        case Availability.OutOfStock        => items.OutOfStock.circeDecoder
                        case Availability.InStock           => items.InStock.circeDecoder
                        case Availability.OutOfStockAnalogs => items.OutOfStockAnalogs.circeDecoder
                        case Availability.PreOrder          => items.PreOrder.circeDecoder
                        case Availability.CannotBeShipped   => items.CannotBeShipped.circeDecoder
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
        availability match {
          case Availability.OutOfStock        =>
            items.OutOfStock(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.InStock           =>
            items.InStock(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, inDays, sellerId,
              addToCartMinItems.get, addToCartMaxItems.get,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.OutOfStockAnalogs =>
            items.OutOfStockAnalogs(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.PreOrder          =>
            items.PreOrder(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
          case Availability.CannotBeShipped   =>
            items.CannotBeShipped(
              itemId, itemIndex, itemType, itemTitle,
              brand, price, rating, categoryPath, delivery, inDays, sellerId,
              isAdult, isAlcohol, isSupermarket, isPersonalized, isPromoted,
              freeRest
            )
        }
      // format: on
    }
}
