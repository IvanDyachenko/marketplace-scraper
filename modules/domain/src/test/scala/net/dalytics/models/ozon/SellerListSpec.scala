package net.dalytics.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class SellerListSpec extends AnyFlatSpec with Matchers {

  it should "decode SellerList.Failure from a valid JSON" in {
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

    decode[SellerList](sellerListRawJson).isRight shouldBe true
  }

  it should "decode SellerList.Success from a valid JSON" in {
    val sellerListRawJson =
      """
        |{
        |  "cms": {
        |    "sellerList": {
        |      "sellerList-438294-default-5": {
        |        "title": "",
        |        "items": [
        |        ],
        |        "view": "list",
        |        "showAllLink": "/seller",
        |        "showAllDeepLink": "ozon://seller",
        |        "trackingInfo": {
        |          "click": {
        |            "actionType": "click",
        |            "key": "42260e602868798c0801ac653563cad1b5c80f56"
        |          },
        |          "view": {
        |            "actionType": "view",
        |            "key": "42260e602868798c0801ac653563cad1b5c80f56"
        |          }
        |        }
        |      }
        |    }
        |  },
        |  "layout": [
        |    {
        |      "vertical": "cms",
        |      "component": "sellerList",
        |      "stateId": "sellerList-438294-default-5",
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
        |        "index": 1,
        |        "timeSpent": 2245292
        |      },
        |      "widgetToken": "5df49407afaf53c1958e7423e9cebb326fbf64d9",
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
        |  "nextPage": "/seller?layout_container=default&layout_page_index=6&page=6",
        |  "pageInfo": {
        |    "url": "/seller?layout_container=default&layout_page_index=5&page=5",
        |    "pageType": "entry_point_seller",
        |    "context": "ozon",
        |    "ruleId": 2744,
        |    "layoutId": 2900,
        |    "layoutVersion": 7
        |  },
        |  "pageToken": "497b81a7165c7a6401f86428ae371fabcd8fc0e3",
        |  "prevPage": "/seller?layout_container=default&layout_page_index=4&page=4",
        |  "shared": {
        |    "context": "ozon",
        |    "layoutId": 2900,
        |    "layoutVersion": 7,
        |    "pageType": "entry_point_seller",
        |    "ruleId": 2744
        |  }
        |}
      """.stripMargin

    decode[SellerList](sellerListRawJson).isRight shouldBe true
  }
}
