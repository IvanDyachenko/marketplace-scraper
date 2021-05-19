package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

import supertagged.postfix._

class LayoutSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode Layout from a valid JSON (tethys)" in {
    val layoutRawJson =
      """
        |[
        |  {
        |    "vertical": "products",
        |    "component": "skuFeedList",
        |    "stateId": "skuFeedList-519255-default-2",
        |    "params": {
        |      "itemsOnPage": 1,
        |      "lowerLimit": 1,
        |      "offset": "searchCPM",
        |      "providerAlgo": "searchCPM",
        |      "title": "Спонсорский товар"
        |    },
        |    "version": 1,
        |    "widgetTrackingInfo": {
        |      "name": "advProductShelf",
        |      "vertical": "products",
        |      "component": "skuFeedList",
        |      "version": 1,
        |      "originName": "rtb.advProductShelf",
        |      "originVertical": "rtb",
        |      "originComponent": "advProductShelf",
        |      "originVersion": 1,
        |      "id": 519255,
        |      "configId": 5710,
        |      "configDtId": 14297,
        |      "revisionId": 658263,
        |      "index": 1,
        |      "dtName": "sku.feedList",
        |      "timeSpent": 12470737
        |    },
        |    "widgetToken": "8113efcaa833949cc85946a53c063b486ef1b485"
        |  },
        |  {
        |    "vertical": "catalog",
        |    "component": "searchResultsV2",
        |    "stateId": "searchResultsV2-189805-default-2",
        |    "version": 1,
        |    "widgetTrackingInfo": {
        |      "name": "catalog.searchResultsV2",
        |      "vertical": "catalog",
        |      "component": "searchResultsV2",
        |      "version": 1,
        |      "id": 189805,
        |      "revisionId": 113249,
        |      "index": 3,
        |      "timeSpent": 124339291
        |    },
        |    "widgetToken": "62ac22fc466fbcb40b8b3cea8a784c2059c981aa"
        |  },
        |  {
        |    "vertical": "catalog",
        |    "component": "soldOutHeader",
        |    "stateId": "soldOutHeader-189812-default-1",
        |    "params": {
        |      "separatorHeight": 2,
        |      "subtitle": "Подпишитесь и мы сообщим о поступлении",
        |      "title": "Товары не в наличии"
        |    },
        |    "version": 1,
        |    "widgetTrackingInfo": {
        |      "name": "catalog.soldOutHeader",
        |      "vertical": "catalog",
        |      "component": "soldOutHeader",
        |      "version": 1,
        |      "id": 189812,
        |      "revisionId": 113257,
        |      "index": 4,
        |      "timeSpent": 1317875980
        |    },
        |    "widgetToken": "f965dd83a441672025b695645cddb5c156853b6a"
        |  },
        |  {
        |    "vertical": "catalog",
        |    "component": "soldOutResultsV2",
        |    "stateId": "soldOutResultsV2-189813-default-1",
        |    "version": 1,
        |    "widgetTrackingInfo": {
        |      "name": "catalog.soldOutResultsV2",
        |      "vertical": "catalog",
        |      "component": "soldOutResultsV2",
        |      "version": 1,
        |      "id": 189813,
        |      "revisionId": 113258,
        |      "index": 5,
        |      "timeSpent": 1317878344
        |    },
        |    "widgetToken": "2f02301ef2af98bd6b8ceed94f4d48de47ab2e03"
        |  },
        |  {
        |    "vertical": "cms",
        |    "component": "sellerList",
        |    "stateId": "sellerList-438294-default-3",
        |    "version": 1,
        |    "widgetTrackingInfo": {
        |      "name": "marketing.sellerList",
        |      "vertical": "cms",
        |      "component": "sellerList",
        |      "version": 1,
        |      "originName": "marketing.sellerList",
        |      "originVertical": "marketing",
        |      "originComponent": "sellerList",
        |      "originVersion": 1,
        |      "id": 438294,
        |      "revisionId": 480225,
        |      "index": 1,
        |      "timeSpent": 5805892
        |    },
        |    "widgetToken": "w-8b2b5815526f6be847148f30471bad24e50296e5",
        |    "trackingOn": true
        |  },
        |  {
        |    "vertical": "catalog",
        |    "component": "categoryMenu",
        |    "stateId": "categoryMenu-281296-default-1",
        |    "version": 1,
        |    "widgetTrackingInfo": {
        |      "name": "catalog.categoryMenu",
        |      "vertical": "catalog",
        |      "component": "categoryMenu",
        |      "version": 1,
        |      "id": 281296,
        |      "revisionId": 472482,
        |      "index": 1,
        |      "timeSpent": 231509626
        |    },
        |    "widgetToken": ""
        |  }
        |]
      """.stripMargin

    val layout = layoutRawJson.jsonAs[Layout].value

    layout should be(
      Layout(
        List(
          Component.SearchResultsV2("searchResultsV2-189805-default-2" @@ Component.StateId),
          Component.SoldOutResultsV2("soldOutResultsV2-189813-default-1" @@ Component.StateId),
          Component.SellerList("sellerList-438294-default-3" @@ Component.StateId),
          Component.CategoryMenu("categoryMenu-281296-default-1" @@ Component.StateId)
        )
      )
    )
  }
}
