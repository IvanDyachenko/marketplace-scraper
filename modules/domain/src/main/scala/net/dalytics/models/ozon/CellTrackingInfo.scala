package net.dalytics.models.ozon

import tethys.JsonReader
import tethys.derivation.semiauto._
import tethys.derivation.builder.ReaderBuilder

private[ozon] final case class CellTrackingInfo(
  itemId: Item.Id,
  itemType: Item.Type,
  itemIndex: Int,
  itemTitle: String,
  brandId: Brand.Id,
  brandName: Brand.Name,
  categoryPath: Category.Path,
  ratingValue: Double,
  ratingCount: Int,
  priceInit: Price.Value,
  priceFinal: Price.Value,
  priceDiscount: Price.Percent,
  availability: Short,
  availableInDays: Short,
  marketplaceSellerId: MarketplaceSeller.Id,
  deliverySchema: Delivery.Schema,
  deliveryTimeDiffDays: Option[Short],
  isSupermarket: Boolean,
  isPersonalized: Boolean,
  isPromotedProduct: Boolean,
  freeRest: Int,
  stockCount: Int
) {
  def brand: Brand       = Brand(brandId, brandName)
  def price: Price       = Price(priceInit, priceFinal, priceDiscount)
  def rating: Rating     = Rating(ratingValue, ratingCount)
  def delivery: Delivery = Delivery(deliverySchema, deliveryTimeDiffDays)
}

private[ozon] object CellTrackingInfo {
  implicit val tethysReader: JsonReader[CellTrackingInfo] =
    jsonReader[CellTrackingInfo] {
      describe {
        // format: off
        ReaderBuilder[CellTrackingInfo]
          .extract(_.itemId).from("id".as[Item.Id])(identity)
          .extract(_.itemType).from("type".as[Item.Type])(identity)
          .extract(_.itemIndex).from("index".as[Int])(identity)
          .extract(_.itemTitle).from("title".as[String])(identity)
          .extract(_.brandName).from("brand".as[Brand.Name])(identity)
          .extract(_.categoryPath).from("category".as[Category.Path])(identity)
          .extract(_.ratingValue).from("rating".as[Double])(identity)
          .extract(_.ratingCount).from("countItems".as[Int])(identity)
          .extract(_.priceInit).from("price".as[Price.Value])(identity)
          .extract(_.priceFinal).from("finalPrice".as[Price.Value])(identity)
          .extract(_.priceDiscount).from("discount".as[Price.Percent])(identity)
        // format: on
      }
    }
}
