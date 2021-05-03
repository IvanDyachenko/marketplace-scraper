package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import tofu.syntax.loggable._
import vulcan.Codec
import vulcan.generic.AvroNamespace
import io.circe.{Decoder, DecodingFailure, HCursor}
import tethys.JsonReader
import tethys.readers.{FieldName, ReaderError}
import tethys.enumeratum.TethysEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Lowercase
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

@derive(loggable)
final case class Item(
  id: Item.Id,
  index: Int,
  `type`: Item.Type,
  title: String,
  brand: Brand,
  price: Price,
  rating: Rating,
  categoryPath: Category.Path,
  delivery: Delivery,
  availability: Short,
  availableInDays: Short,
  marketplaceSellerId: MarketplaceSeller.Id,
  addToCart: AddToCart,
  isAdult: Boolean,
  isAlcohol: Boolean,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean
)

object Item {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Id = Id.Type

  @AvroNamespace("ozon.models.item")
  sealed trait Type extends EnumEntry with Lowercase with Product with Serializable
  object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with TethysEnum[Type] with VulcanEnum[Type] with LoggableEnum[Type] {
    val values = findValues

    case object SKU extends Type
  }

  @AvroNamespace("ozon.models.item")
  sealed abstract class Availability extends EnumEntry
  object Availability                extends Enum[Availability] with CirceEnum[Availability] with TethysEnum[Availability] with VulcanEnum[Availability] {
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

  private def addToCart(availability: Availability, template: Template): Option[AddToCart] =
    availability match {
      case Availability.PreOrder        => Some(AddToCart.Unavailable)
      case Availability.CannotBeShipped => Some(AddToCart.Unavailable)
      case Availability.OutOfStock      => Some(AddToCart.With(0, 0))
      case Availability.InStock         => AddToCart(template)
    }

  implicit val circeDecoder: Decoder[Item] = Decoder.instance[Item] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      availability <- i.get[Short]("availability")
      addToCart    <- Availability.from(availability) match {
                        case Availability.PreOrder        => AddToCart.Unavailable.asRight[DecodingFailure]
                        case Availability.InStock         => c.as[AddToCart]
                        case Availability.OutOfStock      => AddToCart.With(0, 0).asRight[DecodingFailure]
                        case Availability.CannotBeShipped => AddToCart.Unavailable.asRight[DecodingFailure]
                      }
      item         <- (
                        i.get[Item.Id]("id"),
                        i.get[Int]("index"),
                        i.get[Item.Type]("type"),
                        i.get[String]("title"),
                        i.as[Brand],
                        i.as[Price],
                        i.as[Rating],
                        i.get[Category.Path]("category"),
                        i.as[Delivery],
                        Right(availability),
                        i.get[Short]("availableInDays"),
                        i.get[MarketplaceSeller.Id]("marketplaceSellerId"),
                        Right(addToCart),
                        c.get[Boolean]("isAdult"),
                        c.get[Boolean]("isAlcohol"),
                        i.get[Boolean]("isSupermarket"),
                        i.get[Boolean]("isPersonalized"),
                        i.get[Boolean]("isPromotedProduct")
                      ).mapN(apply)
    } yield item
  }

  implicit val tethysReader: JsonReader[Item] =
    JsonReader.builder
      .addField[CellTrackingInfo]("cellTrackingInfo")
      .addField[Template]("templateState")
      .addField[Boolean]("isAdult")
      .addField[Boolean]("isAlcohol")
      .buildReader { (info, template, isAdult, isAlcohol) =>
        val addToCart = Item.addToCart(Availability.from(info.availability), template).getOrElse {
          val message = s"Decoded value of 'templateState' object doesn't contain description of the 'add to cart' action: ${template.logShow}."
          ReaderError.wrongJson(message)(FieldName.apply("templateState"))
        }

        Item(
          info.itemId,
          info.itemIndex,
          info.itemType,
          info.itemTitle,
          info.brand,
          info.price,
          info.rating,
          info.categoryPath,
          info.delivery,
          info.availability,
          info.availableInDays,
          info.marketplaceSellerId,
          addToCart,
          isAdult,
          isAlcohol,
          info.isSupermarket,
          info.isPersonalized,
          info.isPromotedProduct
        )
      }

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
      field("isSupermarket", f(_).isSupermarket),
      field("isPersonalized", f(_).isPersonalized),
      field("isPromotedProduct", f(_).isPromotedProduct)
    ).mapN(apply)
}
