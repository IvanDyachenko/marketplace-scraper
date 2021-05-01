package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class MarketplaceSellerSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode MarketplaceSeller from a valid JSON (circe)" in {
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
        |  "items": [],
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

    val marketplaceSeller = decode[MarketplaceSeller](marketplaceSellerRawJson).value

    marketplaceSeller should be(MarketplaceSeller(314L @@ MarketplaceSeller.Id, "UniStor", "Taking care of your space"))
  }

  it should "decode MarketplaceSeller from a valid JSON (1) (tethys)" in {
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
        |  "items": [],
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

    val marketplaceSeller = marketplaceSellerRawJson.jsonAs[MarketplaceSeller].value

    marketplaceSeller should be(MarketplaceSeller(314L @@ MarketplaceSeller.Id, "UniStor", "Taking care of your space"))
  }

  it should "decode MarketplaceSeller from a valid JSON (2) (tethys)" in {
    val marketplaceSellerRawJson =
      """
        |{
        |  "id": 314,
        |  "title": "UniStor",
        |  "logoImage": "https://cdn1.ozone.ru/s3/marketing-api/banners/rE/bz/rEbzyB3nHw2V3b90lmwkkY9qfNXpLi9a.jpg",
        |  "backgroundImage": "https://cdn1.ozone.ru/s3/marketing-api/banners/1F/5C/1F5CBuIErKeuM47SYq1kWNucnYuKYg8d.jpg",
        |  "backgroundType": "image",
        |  "backgroundColor": "",
        |  "deeplink": "ozon://seller/yunistor-314/?miniapp=seller_314",
        |  "link": "/seller/yunistor-314/",
        |  "isFavorite": false,
        |  "items": [],
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

    val marketplaceSeller = marketplaceSellerRawJson.jsonAs[MarketplaceSeller].value

    marketplaceSeller should be(MarketplaceSeller(314L @@ MarketplaceSeller.Id, "UniStor", "UniStor"))
  }
}
