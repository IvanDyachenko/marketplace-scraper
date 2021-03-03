package net.dalytics.models.ozon.items

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

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
  addToCartMinItems: Int,
  addToCartMaxItems: Int,
  isAdult: Boolean,
  isAlcohol: Boolean,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int
) extends Item

object InStock {
  implicit val circeDecoder: Decoder[InStock] = Decoder.instance[InStock] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      availability <- i.get[Short]("availability")
                        .ensure {
                          val message = s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Item.Availability.InStock}'"
                          DecodingFailure(message, c.history)
                        }(Item.Availability.from(_) == Item.Availability.InStock)
      addToCart     = c.get[Template]("templateState")
                        .flatMap(_.addToCart.fold[Decoder.Result[(Int, Int)]] {
                          val message = "'templateState' doesn't contain an object which describes 'addToCart...' action"
                          Left(DecodingFailure(message, c.history))
                        }(Right(_)))
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
