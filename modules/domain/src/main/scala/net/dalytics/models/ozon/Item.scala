package net.dalytics.models.ozon

import cats.implicits._
import cats.Foldable
import cats.free.FreeApplicative
import tofu.syntax.loggable._
import tofu.logging.{Loggable, LoggableEnum}
import vulcan.Codec
import vulcan.generic.AvroNamespace
import io.circe.{Decoder, DecodingFailure, HCursor, Json}
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
  def addToCart: Item.AddToCart
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

  sealed trait AddToCart {
    def isRedirect: Boolean
  }

  object AddToCart {
    final object Redirect extends AddToCart {
      val isRedirect: Boolean = true
    }

    final object Unavailable extends AddToCart {
      val isRedirect: Boolean = false
    }

    final case class With(minItems: Int, maxItems: Int) extends AddToCart {
      val isRedirect: Boolean = false
    }

    implicit val circeDecoder: Decoder[AddToCart] = Decoder.instance[AddToCart] { (c: HCursor) =>
      for {
        templateJson <- c.get[Json]("templateState")
        template     <- templateJson.as[Template]
        addToCart    <- template.addToCart.fold[Decoder.Result[AddToCart]] {
                          val message = List(
                            s"'templateState' doesn't contain an object which describes 'add to cart', nor 'redirect' actions.",
                            s"Decoded value of ${templateJson.noSpacesSortKeys} is ${template.logShow}."
                          ).mkString(" ")
                          Left(DecodingFailure(message, c.history))
                        }(Right(_))
      } yield addToCart
    }

    implicit val loggable: Loggable[AddToCart] = Loggable.empty

    implicit final class TemplateOps(private val template: Template) extends AnyVal {
      import net.dalytics.models.ozon.Template.State.{Action, MobileContainer}
      import net.dalytics.models.ozon.Template.State.Action.{AddToCartWithCount, UniversalAction}

      def addToCart: Option[AddToCart] =
        template.states.collectFirst {
          case Action.Redirect                                                                   => Redirect
          case AddToCartWithCount(minItems, maxItems)                                            => With(minItems, maxItems)
          case UniversalAction(UniversalAction.Button.AddToCartWithQuantity(quantity, maxItems)) => With(quantity, maxItems)
          case MobileContainer(_, _, footer) if footer.addToCart.isDefined                       => footer.addToCart.get
        }
    }

    def aggregate[C[_]: Foldable](items: C[Item]): Sale = {
      val listOfMaxItems = Foldable[C].toList(items).map(_.addToCart).collect { case Item.AddToCart.With(_, maxItems) =>
        maxItems
      }

      if (listOfMaxItems.length > 1) {
        val numberOfSoldItems =
          listOfMaxItems.tail
            .foldLeft((0, listOfMaxItems.head)) { case ((numberOfSoldItems, prevMaxItems), currMaxItems) =>
              (numberOfSoldItems + 0.max(currMaxItems - prevMaxItems), currMaxItems)
            }
            ._1
        Sale.Sold(numberOfSoldItems)
      } else
        Sale.Unknown
    }

    private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => AddToCart): FreeApplicative[Codec.Field[A, *], AddToCart] =
      (
        field(
          "addToCartIsRedirect",
          f(_) match {
            case Unavailable => None
            case Redirect    => Some(true)
            case With(_, _)  => Some(false)
          }
        ),
        field(
          "addToCartMinItems",
          f(_) match {
            case With(minItems, _) => Some(minItems)
            case _                 => None
          }
        ),
        field(
          "addToCartMaxItems",
          f(_) match {
            case With(_, maxItems) => Some(maxItems)
            case _                 => None
          }
        )
      ).mapN {
        case (Some(true), None, None)                      => Redirect
        case (Some(false), Some(minItems), Some(maxItems)) => With(minItems, maxItems)
        case _                                             => Unavailable
      }
  }

  implicit val loggable: Loggable[Item] = Loggable.empty

  implicit val circeDecoder: Decoder[Item] = Decoder.instance[Item] { (c: HCursor) =>
    for {
      availability <- c.downField("cellTrackingInfo").get[Byte]("availability")
      decoder       = Availability.from(availability) match {
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
