package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class SellerListSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode SellerList.Failure from a valid JSON (circe)" in {
    val sellerListRawJson =
      """
        |{
        |  "cms": {
        |    "sellerList": {
        |      "sellerList-438294-default-500": {
        |        "error": "internal server error"
        |      }
        |    }
        |  },
        |  "layout": [
        |    {
        |      "vertical": "cms",
        |      "component": "sellerList",
        |      "stateId": "sellerList-438294-default-500",
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "marketing.sellerList",
        |        "vertical": "cms",
        |        "component": "sellerList",
        |        "version": 1,
        |        "originName": "marketing.sellerList",
        |        "originVertical": "marketing",
        |        "originComponent": "sellerList",
        |        "originVersion": 1,
        |        "id": 438294,
        |        "revisionId": 480225,
        |        "index": 1
        |      },
        |      "widgetToken": "124a0903d08b9c5f8d42f927b4b1e022ed8c24be",
        |      "trackingOn": true
        |    }
        |  ],
        |  "layoutTrackingInfo": {
        |    "deviceType": "app",
        |    "layoutId": 2900,
        |    "layoutVersion": 7,
        |    "pageType": "entry_point_seller",
        |    "platform": "mobile_site",
        |    "ruleId": 2744,
        |    "templateType": "mobile"
        |  },
        |  "pageInfo": {
        |    "url": "/seller?layout_container=default&layout_page_index=500&page=500",
        |    "pageType": "entry_point_seller",
        |    "context": "ozon",
        |    "ruleId": 2744,
        |    "layoutId": 2900,
        |    "layoutVersion": 7
        |  },
        |  "pageToken": "497b81a7165c7a6401f86428ae371fabcd8fc0e3",
        |  "shared": {
        |    "context": "ozon",
        |    "layoutId": 2900,
        |    "layoutVersion": 7,
        |    "pageType": "entry_point_seller",
        |    "ruleId": 2744
        |  },
        |  "trackingPayloads": {
        |  }
        |}
      """.stripMargin

    val result = decode[Result](sellerListRawJson).value

    result.sellerList.value shouldBe a[SellerList]
  }

  it should "decode SellerList.Failure from a valid JSON (tethys)" in {
    val sellerListRawJson =
      """
        |{
        |  "sellerList-438294-default-500": {
        |    "error": "internal server error"
        |  }
        |}
      """.stripMargin

    val component           = Component.SellerList("sellerList-438294-default-500" @@ Component.StateId)
    implicit val jsonReader = SellerList.tethysJsonReader(component)
    val sellerList          = sellerListRawJson.jsonAs[SellerList].value

    sellerList should be(SellerList.Failure("internal server error"))
  }

  it should "decode SellerList.Success from a valid JSON (tethys)" in {
    val sellerListRawJson =
      """
        |{
        |  "sellerList-438294-default-5": {
        |    "title": "",
        |    "items": [],
        |    "view": "list",
        |    "showAllLink": "/seller",
        |    "showAllDeepLink": "ozon://seller",
        |    "trackingInfo": {
        |      "click": {
        |        "actionType": "click",
        |        "key": "42260e602868798c0801ac653563cad1b5c80f56"
        |      },
        |      "view": {
        |        "actionType": "view",
        |        "key": "42260e602868798c0801ac653563cad1b5c80f56"
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    val component           = Component.SellerList("sellerList-438294-default-5" @@ Component.StateId)
    implicit val jsonReader = SellerList.tethysJsonReader(component)
    val sellerList          = sellerListRawJson.jsonAs[SellerList].value

    sellerList should be(SellerList.Success(List.empty))
  }
}
