package marketplace.models.ozon.items

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

import marketplace.models.ozon.{Brand, Category, Delivery, Item, MarketplaceSeller, Price, Rating}

@derive(loggable)
final case class OutOfStock(
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
  isAdult: Boolean,
  isAlcohol: Boolean,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int
) extends Item {
  val availability: Item.Availability = Item.Availability.OutOfStock
}

object OutOfStock {
  implicit val circeDecoder: Decoder[OutOfStock] = Decoder.instance[OutOfStock] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      _    <- i.get[Item.Availability]("availability")
                .ensure {
                  val message =
                    s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Item.Availability.OutOfStock.value}'"
                  DecodingFailure(message, c.history)
                }(_ == Item.Availability.OutOfStock)
      item <- (
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
