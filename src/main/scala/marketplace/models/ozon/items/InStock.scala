package marketplace.models.ozon.items

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

import marketplace.models.ozon.{Brand, Category, Delivery, Item, MarketplaceSeller, Price, Rating, Template}

@derive(loggable)
final case class InStock(
  id: Item.Id,
  index: Int,
  `type`: Item.Type,
  title: String,
  brand: Brand,
  price: Price,
  rating: Rating,
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
  val availability: Item.Availability = Item.Availability.InStock
}

object InStock {
  implicit val circeDecoder: Decoder[InStock] = Decoder.instance[InStock] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      _        <- i.get[Item.Availability]("availability")
                    .ensure {
                      val message = s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Item.Availability.InStock.value}'"
                      DecodingFailure(message, c.history)
                    }(_ == Item.Availability.InStock)
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
