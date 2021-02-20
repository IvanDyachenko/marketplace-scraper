package marketplace.models.ozon.items

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

import marketplace.models.ozon.{Brand, Category, Delivery, Item, MarketplaceSeller, Price, Rating}

@derive(loggable)
final case class OutOfStock private (
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
  isAdult: Boolean,
  isAlcohol: Boolean,
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int
) extends Item

object OutOfStock {
  implicit val circeDecoder: Decoder[OutOfStock] = Decoder.instance[OutOfStock] { (c: HCursor) =>
    lazy val i = c.downField("cellTrackingInfo")

    for {
      availability <- i.get[Short]("availability")
                        .ensure {
                          val message =
                            s"'cellTrackingInfo' doesn't contain 'availability' which is equal to '${Item.Availability.OutOfStock}'"
                          DecodingFailure(message, c.history)
                        }(Item.Availability.from(_) == Item.Availability.OutOfStock)
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
