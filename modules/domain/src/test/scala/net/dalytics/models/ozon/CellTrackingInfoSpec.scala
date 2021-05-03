package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

import supertagged.postfix._

class CellTrackingInfoSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode CellTrackingInfo from a valid JSON (tethys)" in {
    val cellTrackingInfoRawJson =
      """
        |{
        |  "index": 1369,
        |  "type": "sku",
        |  "id": 229603477,
        |  "title": "Футболка YELLOW PRICE",
        |  "availability": 1,
        |  "price": 1600,
        |  "finalPrice": 999,
        |  "deliverySchema": "FBS",
        |  "marketplaceSellerId": 102155,
        |  "category": "Одежда, обувь и аксессуары/Женщинам/Одежда/Футболки и топы/Футболки/YELLOW PRICE",
        |  "brand": "YELLOW PRICE",
        |  "brandId": 87327572,
        |  "availableInDays": 0,
        |  "freeRest": 5,
        |  "stockCount": 5,
        |  "discount": 37,
        |  "marketingActionIds": [
        |    11010979456230,
        |    11050497706160
        |  ],
        |  "isPersonalized": false,
        |  "deliveryTimeDiffDays": 3,
        |  "isSupermarket": false,
        |  "isPromotedProduct": false,
        |  "status": "цена с Premium",
        |  "rating": 0,
        |  "countItems": 0,
        |  "customDimension4": "Доставит Ozon",
        |  "availableDeliverySchema": [
        |    221
        |  ],
        |  "credit_product_type": "credit,6",
        |  "credit_product_price": 179
        |}
      """.stripMargin

    val cellTrackingInfo = cellTrackingInfoRawJson.jsonAs[CellTrackingInfo].value

    cellTrackingInfo should be(
      CellTrackingInfo(
        itemId = 229603477L @@ Item.Id,
        itemType = Item.Type.SKU,
        itemIndex = 1369,
        itemTitle = "Футболка YELLOW PRICE",
        brandId = 87327572L @@ Brand.Id,
        brandName = "YELLOW PRICE" @@ Brand.Name,
        categoryPath = "Одежда, обувь и аксессуары/Женщинам/Одежда/Футболки и топы/Футболки/YELLOW PRICE" @@ Category.Path,
        ratingValue = 0,
        ratingCount = 0,
        priceInit = 1600d @@ Price.Value,
        priceFinal = 999d @@ Price.Value,
        priceDiscount = (37: Byte) @@ Price.Percent,
        availability = 1,
        availableInDays = 0,
        marketplaceSellerId = 102155L @@ MarketplaceSeller.Id,
        deliverySchema = Delivery.Schema.FBS,
        deliveryTimeDiffDays = Some(3: Short),
        isSupermarket = false,
        isPersonalized = false,
        isPromotedProduct = false
      )
    )
  }
}
