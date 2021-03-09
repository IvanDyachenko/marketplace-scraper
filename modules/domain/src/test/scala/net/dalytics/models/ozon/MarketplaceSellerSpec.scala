package net.dalytics.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class MarketplaceSellerSpec extends AnyFlatSpec with Matchers {

  it should "decode MarketplaceSeller from a valid JSON" in {
    val marketplaceSellerRawJson =
      """
        |{
        |  "id": 314,
        |  "title": "UniStor",
        |  "subtitle": "Taking care of your space",
        |  "logoImage": "https://cdn1.ozone.ru/s3/marketing-api/banners/rE/bz/rEbzyB3nHw2V3b90lmwkkY9qfNXpLi9a.jpg",
        |  "backgroundImage": "https://cdn1.ozone.ru/s3/marketing-api/banners/1F/5C/1F5CBuIErKeuM47SYq1kWNucnYuKYg8d.jpg",
        |  "backgroundType": "image",
        |  "backgroundColor": "",
        |  "deeplink": "ozon://seller/yunistor-314/?miniapp=seller_314",
        |  "link": "/seller/yunistor-314/",
        |  "isFavorite": false,
        |  "items": [
        |    {
        |      "sku": 163204456,
        |      "title": "Ершик для унитаза UniStor",
        |      "image": "https://cdn1.ozone.ru/s3/multimedia-b/6006833711.jpg",
        |      "isAdult": false,
        |      "price": 296,
        |      "discount": 23,
        |      "finalPrice": 227,
        |      "link": "/seller/yunistor-314/",
        |      "deeplink": "ozon://seller/yunistor-314/?miniapp=seller_314"
        |    },
        |    {
        |      "sku": 163349018,
        |      "title": "Ершик для унитаза UniStor",
        |      "image": "https://cdn1.ozone.ru/s3/multimedia-7/6006830683.jpg",
        |      "isAdult": false,
        |      "price": 500,
        |      "discount": 23,
        |      "finalPrice": 384,
        |      "link": "/seller/yunistor-314/",
        |      "deeplink": "ozon://seller/yunistor-314/?miniapp=seller_314"
        |    },
        |    {
        |      "sku": 163214590,
        |      "title": "Сушилка для посуды UniStor , 48 см х 27 см х 11 см",
        |      "image": "https://cdn1.ozone.ru/s3/multimedia-6/6006823950.jpg",
        |      "isAdult": false,
        |      "price": 1265,
        |      "discount": 23,
        |      "finalPrice": 973,
        |      "link": "/seller/yunistor-314/",
        |      "deeplink": "ozon://seller/yunistor-314/?miniapp=seller_314"
        |    }
        |  ],
        |  "utm": "",
        |  "advId": "",
        |  "trackingInfo": {
        |    "click": {
        |      "actionType": "click",
        |      "key": "6709b984f7d8ccb98c4b674409969b9ced28468f"
        |    },
        |    "view": {
        |      "actionType": "view",
        |      "key": "6709b984f7d8ccb98c4b674409969b9ced28468f"
        |    }
        |  }
        |}
      """.stripMargin

    decode[MarketplaceSeller](marketplaceSellerRawJson).isRight shouldBe true
  }
}
