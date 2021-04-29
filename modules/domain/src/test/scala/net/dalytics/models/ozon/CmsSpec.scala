package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

import supertagged.postfix._

class CmsSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode Cms from a valid JSON (1) (tethys)" in {
    val cmsRawJson =
      """
        |{
        |  "sellerList": {
        |    "sellerList-438294-default-3": {
        |      "title": "Все магазины",
        |      "items": [
        |        {
        |          "id": 15116,
        |          "title": "АЛТАСИЛ",
        |          "subtitle": "МУЖСКОЕ ЗДОРОВЬЕ",
        |          "logoImage": "https://cdn1.ozone.ru/s3/marketing-api/banners/Qv/TH/QvTHVdiAFxwQDUDyaGGzaZ1RrtnVYm8s.jpg",
        |          "backgroundType": "image",
        |          "deeplink": "ozon://seller/ekspress-sistema-15116/?miniapp=seller_15116",
        |          "link": "/seller/ekspress-sistema-15116/",
        |          "isFavorite": false,
        |          "items": [],
        |          "utm": "BTxwM25d6VbsP6UvAtcBtCIbUJ7uAIwa1lqOlT2O",
        |          "advId": "BTxwM25d6VbsP6UvAtcBtCIbUJ7uAIwa1lqOlT2O",
        |          "trackingInfo": {
        |            "click": {
        |              "actionType": "click",
        |              "key": "a-b538756c0cf6d095f9af41dafc4253fa8385149f"
        |            },
        |            "view": {
        |              "actionType": "view",
        |              "key": "a-b538756c0cf6d095f9af41dafc4253fa8385149f"
        |            }
        |          },
        |          "ratingBadge": {
        |            "text": "4.5 из 5 рейтинг"
        |          }
        |        }
        |      ],
        |      "view": "list",
        |      "showAllLink": "/seller",
        |      "showAllDeepLink": "ozon://seller",
        |      "trackingInfo": {
        |        "click": {
        |          "actionType": "click",
        |          "key": "a-b2151ad39454c0f462d7210670be2419bf29f85d"
        |        },
        |        "view": {
        |          "actionType": "view",
        |          "key": "a-b2151ad39454c0f462d7210670be2419bf29f85d"
        |        }
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    val expectedSellerList = SellerList.Success(List(MarketplaceSeller(15116L @@ MarketplaceSeller.Id, "АЛТАСИЛ", "МУЖСКОЕ ЗДОРОВЬЕ")))
    val expectedCms        = Cms(Some(expectedSellerList))

    val component           = Component.SellerList("sellerList-438294-default-3" @@ Component.StateId)
    val layout              = Layout(List(component))
    implicit val jsonReader = Cms.tethysJsonReader(layout)
    val decodedCms          = cmsRawJson.jsonAs[Cms].value

    decodedCms should be(expectedCms)
  }
}
