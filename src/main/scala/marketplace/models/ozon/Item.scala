package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.values.{ByteCirceEnum, ByteEnum, ByteEnumEntry, ByteVulcanEnum}
import enumeratum.EnumEntry.Lowercase
import vulcan.Codec
import io.circe.{Decoder, DecodingFailure, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
sealed trait Item {
  def id: Item.Id
  def index: Int
  def `type`: Item.Type
  def title: String
  def brand: Brand
  def price: Price
  def rating: Rating
  def category: Category
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

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable

  object Type extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] with VulcanEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

  sealed abstract class Availability(val value: Byte) extends ByteEnumEntry

  object Availability extends ByteEnum[Availability] with ByteCirceEnum[Availability] with ByteVulcanEnum[Availability] {
    val values = findValues

    case object InStock    extends Availability(1)
    case object OutOfStock extends Availability(2)
  }

  @derive(loggable)
  final case class InStock(
    id: Id,
    index: Int,
    `type`: Type,
    title: String,
    brand: Brand,
    price: Price,
    rating: Rating,
    category: Category,
    categoryPath: Category.Path,
    delivery: Delivery,
    availableInDays: Short,
    marketplaceSellerId: MarketplaceSeller.Id,
    addToCartMinItems: Int,
    addToCartMaxItems: Int,
    isAdult: Boolean,
    isAlcohol: Boolean,
    isSupermarket: Boolean,
    isPersonalized: Boolean,
    isPromotedProduct: Boolean,
    freeRest: Int
  ) extends Item {
    val availability: Availability = Availability.InStock
  }

  @derive(loggable)
  final case class OutOfStock(
    id: Id,
    index: Int,
    `type`: Type,
    title: String,
    brand: Brand,
    price: Price,
    rating: Rating,
    category: Category,
    categoryPath: Category.Path,
    delivery: Delivery,
    availableInDays: Short,
    marketplaceSellerId: MarketplaceSeller.Id,
    isAdult: Boolean,
    isAlcohol: Boolean,
    isSupermarket: Boolean,
    isPersonalized: Boolean,
    isPromotedProduct: Boolean,
    freeRest: Int
  ) extends Item {
    val availability: Availability = Availability.OutOfStock
  }

  object InStock {
    implicit def circeDecoder(category: Category): Decoder[InStock] = Decoder.instance[InStock] { (c: HCursor) =>
      lazy val i = c.downField("cellTrackingInfo")

      for {
        _        <- i.get[Availability]("availability")
                      .ensure {
                        val message = s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Availability.InStock.value}'"
                        DecodingFailure(message, c.history)
                      }(_ == Availability.InStock)
        addToCart = c.get[Template]("templateState")
                      .flatMap(_.addToCart.fold[Decoder.Result[(Int, Int)]] {
                        val message = "'templateState' doesn't contain an object which describes 'addToCart...' action"
                        Left(DecodingFailure(message, c.history))
                      }(Right(_)))
        item     <- (
                      i.get[Item.Id]("id"),
                      i.get[Int]("index"),
                      i.get[Item.Type]("type"),
                      i.get[String]("title"),
                      i.as[Brand],
                      i.as[Price],
                      i.as[Rating],
                      category.asRight[DecodingFailure],
                      i.get[Category.Path]("category"),
                      i.as[Delivery],
                      i.get[Short]("availableInDays"),
                      i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
                      addToCart.map(_._1),
                      addToCart.map(_._2),
                      c.get[Boolean]("isAdult"),
                      c.get[Boolean]("isAlcohol"),
                      i.get[Boolean]("isSupermarket"),
                      i.get[Boolean]("isPersonalized"),
                      i.get[Boolean]("isPromotedProduct"),
                      i.get[Int]("freeRest")
                    ).mapN(apply)
      } yield item
    }
  }

  object OutOfStock {
    implicit def circeDecoder(category: Category): Decoder[OutOfStock] = Decoder.instance[OutOfStock] { (c: HCursor) =>
      lazy val i = c.downField("cellTrackingInfo")

      for {
        _    <- i.get[Availability]("availability")
                  .ensure {
                    val message = s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Availability.OutOfStock.value}'"
                    DecodingFailure(message, c.history)
                  }(_ == Availability.OutOfStock)
        item <- (
                  i.get[Item.Id]("id"),
                  i.get[Int]("index"),
                  i.get[Item.Type]("type"),
                  i.get[String]("title"),
                  i.as[Brand],
                  i.as[Price],
                  i.as[Rating],
                  category.asRight[DecodingFailure],
                  i.get[Category.Path]("category"),
                  i.as[Delivery],
                  i.get[Short]("availableInDays"),
                  i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
                  c.get[Boolean]("isAdult"),
                  c.get[Boolean]("isAlcohol"),
                  i.get[Boolean]("isSupermarket"),
                  i.get[Boolean]("isPersonalized"),
                  i.get[Boolean]("isPromotedProduct"),
                  i.get[Int]("freeRest")
                ).mapN(apply)
      } yield item
    }
  }

  implicit def circeDecoder(category: Category): Decoder[Item] = Decoder.instance[Item] { (c: HCursor) =>
    for {
      availability <- c.downField("cellTrackingInfo").get[Availability]("availability")
      decoder       = availability match {
                        case Availability.InStock    => InStock.circeDecoder(category)
                        case Availability.OutOfStock => OutOfStock.circeDecoder(category)
                      }
      item         <- decoder.widen[Item](c)
    } yield item
  }

  // FixMe :(
  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Item): FreeApplicative[Codec.Field[A, *], Item] =
    (
      field("itemId", f(_).id),
      field("itemIndex", f(_).index),
      field("itemType", f(_).`type`),
      field("itemTitle", f(_).title),
      Brand.vulcanCodecFieldFA(field)(f(_).brand),
      Price.vulcanCodecFieldFA(field)(f(_).price),
      Rating.vulcanCodecFieldFA(field)(f(_).rating),
      Category.vulcanCodecFieldFA(field)(f(_).category),
      field("categoryPath", f(_).categoryPath),
      Delivery.vulcanCodecFieldFA(field)(f(_).delivery),
      field("availability", f(_).availability),
      field("availableInDays", f(_).availableInDays),
      field("marketplaceSellerId", f(_).marketplaceSellerId),
      field("addToCartMinItems", f(_) match { case item: InStock => Some(item.addToCartMinItems); case _ => None }),
      field("addToCartMaxItems", f(_) match { case item: InStock => Some(item.addToCartMaxItems); case _ => None }),
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
            category,
            categoryPath,
            delivery,
            availability,
            availableInDays,
            marketplaceSellerId,
            addToCartMinItems,
            addToCartMaxItems,
            isAdult,
            isAlcohol,
            _,
            isSupermarket,
            isPersonalized,
            isPromotedProduct,
            freeRest
          ) =>
        if (availability == Availability.InStock)
          InStock(
            itemId,
            itemIndex,
            itemType,
            itemTitle,
            brand,
            price,
            rating,
            category,
            categoryPath,
            delivery,
            availableInDays,
            marketplaceSellerId,
            addToCartMinItems.get,
            addToCartMaxItems.get,
            isAdult,
            isAlcohol,
            isSupermarket,
            isPersonalized,
            isPromotedProduct,
            freeRest
          )
        else
          OutOfStock(
            itemId,
            itemIndex,
            itemType,
            itemTitle,
            brand,
            price,
            rating,
            category,
            categoryPath,
            delivery,
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
