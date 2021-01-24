package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.values.{IntCirceEnum, IntEnum, IntEnumEntry, IntVulcanEnum}
import enumeratum.EnumEntry.Lowercase
import vulcan.generic.AvroNamespace
import io.circe.{Decoder, DecodingFailure, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
sealed trait Item {
  def id: Item.Id
  def `type`: Item.Type
  def title: String
  def brand: Brand
  def price: Price
  def rating: Rating
  def categoryName: Category.Name
  def delivery: Delivery
  def availability: Item.Availability
  def availableInDays: Int
  def marketplaceSellerId: MarketplaceSeller.Id
  def isAdult: Boolean
  def isAlcohol: Boolean
  def isAvailable: Boolean = availability == Item.Availability.InStock
  def isSupermarket: Boolean
  def isPersonalized: Boolean
  def isPromotedProduct: Boolean
  def index: Int
  def freeRest: Int
}

object Item {

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  @AvroNamespace("ozon.models.item")
  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable
  object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] with VulcanEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

  @AvroNamespace("ozon.models.item")
  sealed abstract class Availability(val value: Int) extends IntEnumEntry
  object Availability                                extends IntEnum[Availability] with IntCirceEnum[Availability] with IntVulcanEnum[Availability] {
    val values = findValues

    case object InStock    extends Availability(1)
    case object OutOfStock extends Availability(2)
  }

  @derive(loggable)
  final case class InStock(
    id: Item.Id,
    `type`: Item.Type,
    title: String,
    brand: Brand,
    price: Price,
    rating: Rating,
    categoryName: Category.Name,
    delivery: Delivery,
    availableInDays: Int,
    marketplaceSellerId: MarketplaceSeller.Id,
    addToCartQuantity: Int,
    addToCartMaxItems: Int,
    isAdult: Boolean,
    isAlcohol: Boolean,
    isSupermarket: Boolean,
    isPersonalized: Boolean,
    isPromotedProduct: Boolean,
    index: Int,
    freeRest: Int
  ) extends Item {
    val availability: Availability = Availability.InStock
  }

  @derive(loggable)
  final case class OutOfStock(
    id: Item.Id,
    `type`: Item.Type,
    title: String,
    brand: Brand,
    price: Price,
    rating: Rating,
    categoryName: Category.Name,
    delivery: Delivery,
    availableInDays: Int,
    marketplaceSellerId: MarketplaceSeller.Id,
    isAdult: Boolean,
    isAlcohol: Boolean,
    isSupermarket: Boolean,
    isPersonalized: Boolean,
    isPromotedProduct: Boolean,
    index: Int,
    freeRest: Int
  ) extends Item {
    val availability: Availability = Availability.OutOfStock
  }

  object InStock {
    implicit val circeDecoder: Decoder[InStock] = Decoder.instance[InStock] { (c: HCursor) =>
      lazy val i = c.downField("cellTrackingInfo")

      for {
        _        <- i.get[Availability]("availability")
                      .ensure {
                        val message = s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Availability.InStock.value}'"
                        DecodingFailure(message, c.history)
                      }(_ == Availability.InStock)
        addToCart = c.get[Template]("templateState")
                      .flatMap(_.addToCartWithQuantity.fold[Decoder.Result[Template.State.UniversalAction.Button.AddToCartWithQuantity]] {
                        val message = "'templateState' doesn't contain an object which describes 'addToCartWithQuantity' action"
                        Left(DecodingFailure(message, c.history))
                      }(Right(_)))
        item     <- (
                      i.get[Item.Id]("id"),
                      i.get[Item.Type]("type"),
                      i.get[String]("title"),
                      i.as[Brand],
                      i.as[Price],
                      i.as[Rating],
                      i.get[Category.Name]("category"),
                      i.as[Delivery],
                      i.get[Int]("availableInDays"),
                      i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
                      addToCart.map(_.quantity),
                      addToCart.map(_.maxItems),
                      c.get[Boolean]("isAdult"),
                      c.get[Boolean]("isAlcohol"),
                      i.get[Boolean]("isSupermarket"),
                      i.get[Boolean]("isPersonalized"),
                      i.get[Boolean]("isPromotedProduct"),
                      i.get[Int]("index"),
                      i.get[Int]("freeRest")
                    ).mapN(apply)
      } yield item
    }
  }

  object OutOfStock {
    implicit val circeDecoder: Decoder[OutOfStock] = Decoder.instance[OutOfStock] { (c: HCursor) =>
      lazy val i = c.downField("cellTrackingInfo")

      for {
        _    <- i.get[Availability]("availability")
                  .ensure {
                    val message = s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Availability.OutOfStock.value}'"
                    DecodingFailure(message, c.history)
                  }(_ == Availability.OutOfStock)
        item <- (
                  i.get[Item.Id]("id"),
                  i.get[Item.Type]("type"),
                  i.get[String]("title"),
                  i.as[Brand],
                  i.as[Price],
                  i.as[Rating],
                  i.get[Category.Name]("category"),
                  i.as[Delivery],
                  i.get[Int]("availableInDays"),
                  i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
                  c.get[Boolean]("isAdult"),
                  c.get[Boolean]("isAlcohol"),
                  i.get[Boolean]("isSupermarket"),
                  i.get[Boolean]("isPersonalized"),
                  i.get[Boolean]("isPromotedProduct"),
                  i.get[Int]("index"),
                  i.get[Int]("freeRest")
                ).mapN(apply)
      } yield item
    }
  }

  implicit val circeDecoder: Decoder[Item] =
    List[Decoder[Item]](
      Decoder[InStock].widen,
      Decoder[OutOfStock].widen
    ).reduceLeft(_ or _)
}
