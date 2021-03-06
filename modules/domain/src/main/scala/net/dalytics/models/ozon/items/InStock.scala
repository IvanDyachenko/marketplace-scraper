package net.dalytics.models.ozon.items

import cats.implicits._
import derevo.derive
import tofu.syntax.loggable._
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor, Json}

import net.dalytics.models.ozon.{Brand, Category, Delivery, Item, MarketplaceSeller, Price, Rating, Template}

@derive(loggable)
final case class InStock private (
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
  addToCart: InStock.AddToCart,
  isAdult: Boolean,
  isAlcohol: Boolean,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int
) extends Item

object InStock {
  @derive(loggable)
  sealed trait AddToCart {
    def isRedirect: Boolean
  }

  object AddToCart {
    @derive(loggable)
    final object Redirect extends AddToCart {
      val isRedirect: Boolean = true
    }

    @derive(loggable)
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
  }

  implicit val circeDecoder: Decoder[InStock] = Decoder.instance[InStock] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      availability <- i.get[Short]("availability")
                        .ensure {
                          val message = s"'cellTrackingInfo.availability' isn't equal to '${Item.Availability.InStock}'"
                          DecodingFailure(message, c.history)
                        }(Item.Availability.from(_) == Item.Availability.InStock)
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
                        c.as[AddToCart],
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
