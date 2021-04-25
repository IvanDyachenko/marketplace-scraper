package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class ComponentSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Component.SellerList from a valid JSON (circe)" in {
    val componentRawJson =
      """
        |{
        |  "vertical": "cms",
        |  "component": "sellerList",
        |  "stateId": "sellerList-438294-default-400",
        |  "version": 1,
        |  "widgetTrackingInfo": {
        |    "name": "marketing.sellerList",
        |    "vertical": "cms",
        |    "component": "sellerList",
        |    "version": 1,
        |    "originName": "marketing.sellerList",
        |    "originVertical": "marketing",
        |    "originComponent": "sellerList",
        |    "originVersion": 1,
        |    "id": 438294,
        |    "revisionId": 480225,
        |    "index": 1
        |  },
        |  "widgetToken": "124a0903d08b9c5f8d42f927b4b1e022ed8c24be",
        |  "trackingOn": true
        |}
      """.stripMargin

    val component = decode[Component](componentRawJson).value

    component should be(Component.SellerList("sellerList-438294-default-400" @@ Component.StateId))
  }

  it should "decode Component.SellerList from a valid JSON (tethys)" in {
    val componentRawJson =
      """
        |{
        |  "vertical": "cms",
        |  "component": "sellerList",
        |  "stateId": "sellerList-438294-default-400",
        |  "version": 1,
        |  "widgetTrackingInfo": {
        |    "name": "marketing.sellerList",
        |    "vertical": "cms",
        |    "component": "sellerList",
        |    "version": 1,
        |    "originName": "marketing.sellerList",
        |    "originVertical": "marketing",
        |    "originComponent": "sellerList",
        |    "originVersion": 1,
        |    "id": 438294,
        |    "revisionId": 480225,
        |    "index": 1
        |  },
        |  "widgetToken": "124a0903d08b9c5f8d42f927b4b1e022ed8c24be",
        |  "trackingOn": true
        |}
      """.stripMargin

    val component = componentRawJson.jsonAs[Component].value

    component should be(Component.SellerList("sellerList-438294-default-400" @@ Component.StateId))
  }

  it should "decode Component.CategoryMenu from a valid JSON (circe)" in {
    val componentRawJson =
      """
        |{
        |  "vertical": "catalog",
        |  "component": "categoryMenu",
        |  "stateId": "categoryMenu-281296-default-1",
        |  "version": 1,
        |  "widgetTrackingInfo": {
        |    "name": "catalog.categoryMenu",
        |    "vertical": "catalog",
        |    "component": "categoryMenu",
        |    "version": 1,
        |    "id": 281296,
        |    "revisionId": 472482,
        |    "index": 1,
        |    "timeSpent": 28996143
        |  },
        |  "widgetToken": ""
        |}
      """.stripMargin

    val component = decode[Component](componentRawJson).value

    component should be(Component.CategoryMenu("categoryMenu-281296-default-1" @@ Component.StateId))
  }

  it should "decode Component.CategoryMenu from a valid JSON (tethys)" in {
    val componentRawJson =
      """
        |{
        |  "vertical": "catalog",
        |  "component": "categoryMenu",
        |  "stateId": "categoryMenu-281296-default-1",
        |  "version": 1,
        |  "widgetTrackingInfo": {
        |    "name": "catalog.categoryMenu",
        |    "vertical": "catalog",
        |    "component": "categoryMenu",
        |    "version": 1,
        |    "id": 281296,
        |    "revisionId": 472482,
        |    "index": 1,
        |    "timeSpent": 28996143
        |  },
        |  "widgetToken": ""
        |}
      """.stripMargin

    val component = componentRawJson.jsonAs[Component].value

    component should be(Component.CategoryMenu("categoryMenu-281296-default-1" @@ Component.StateId))
  }

  it should "decode Component.SearchResultsV2 from a valid JSON (circe)" in {
    val componentRawJson =
      """
        |{
        |  "vertical" : "catalog",
        |  "widgetToken" : "",
        |  "version" : 1,
        |  "stateId" : "searchResultsV2-189805-default-131",
        |  "component" : "searchResultsV2",
        |  "widgetTrackingInfo" : {
        |    "component" : "searchResultsV2",
        |    "id" : 189805,
        |    "vertical" : "catalog",
        |    "index" : 3,
        |    "revisionId" : 113249,
        |    "name" : "catalog.searchResultsV2",
        |    "version" : 1
        |  }
        |}
      """.stripMargin

    val component = decode[Component](componentRawJson).value

    component should be(Component.SearchResultsV2("searchResultsV2-189805-default-131" @@ Component.StateId))
  }

  it should "decode Component.SearchResultsV2 from a valid JSON (tethys)" in {
    val componentRawJson =
      """
        |{
        |  "vertical" : "catalog",
        |  "widgetToken" : "",
        |  "version" : 1,
        |  "stateId" : "searchResultsV2-189805-default-131",
        |  "component" : "searchResultsV2",
        |  "widgetTrackingInfo" : {
        |    "component" : "searchResultsV2",
        |    "id" : 189805,
        |    "vertical" : "catalog",
        |    "index" : 3,
        |    "revisionId" : 113249,
        |    "name" : "catalog.searchResultsV2",
        |    "version" : 1
        |  }
        |}
      """.stripMargin

    val component = componentRawJson.jsonAs[Component].value

    component should be(Component.SearchResultsV2("searchResultsV2-189805-default-131" @@ Component.StateId))
  }

  it should "decode Component.SoldOutResultsV2 from a valid JSON (circe)" in {
    val componentRawJson =
      """
        |{
        |  "vertical": "catalog",
        |  "component": "soldOutResultsV2",
        |  "stateId": "soldOutResultsV2-189813-default-1",
        |  "version": 1,
        |  "widgetTrackingInfo": {
        |    "name": "catalog.soldOutResultsV2",
        |    "vertical": "catalog",
        |    "component": "soldOutResultsV2",
        |    "version": 1,
        |    "id": 189813,
        |    "revisionId": 113258,
        |    "index": 5,
        |    "timeSpent": 1770986218
        |  },
        |  "widgetToken": ""
        |}
      """.stripMargin

    val component = decode[Component](componentRawJson).value

    component should be(Component.SoldOutResultsV2("soldOutResultsV2-189813-default-1" @@ Component.StateId))
  }

  it should "decode Component.SoldOutResultsV2 from a valid JSON (tethys)" in {
    val componentRawJson =
      """
        |{
        |  "vertical": "catalog",
        |  "component": "soldOutResultsV2",
        |  "stateId": "soldOutResultsV2-189813-default-1",
        |  "version": 1,
        |  "widgetTrackingInfo": {
        |    "name": "catalog.soldOutResultsV2",
        |    "vertical": "catalog",
        |    "component": "soldOutResultsV2",
        |    "version": 1,
        |    "id": 189813,
        |    "revisionId": 113258,
        |    "index": 5,
        |    "timeSpent": 1770986218
        |  },
        |  "widgetToken": ""
        |}
      """.stripMargin

    val component = componentRawJson.jsonAs[Component].value

    component should be(Component.SoldOutResultsV2("soldOutResultsV2-189813-default-1" @@ Component.StateId))
  }

  ignore should "decode Component.UWidgetSKU from a valid JSON (circe)" in {
    val componentRawJson =
      """
        |{
        |  "component" : "uWidgetSKU",
        |  "stateId" : "uWidgetSKU-519261-default-131",
        |  "widgetTrackingInfo" : {
        |    "version" : 1,
        |    "revisionId" : 568048,
        |    "name" : "advProductShelf",
        |    "id" : 519261,
        |    "originComponent" : "advProductShelf",
        |    "originVersion" : 1,
        |    "originVertical" : "rtb",
        |    "configId" : 5710,
        |    "index" : 1,
        |    "originName" : "rtb.advProductShelf",
        |    "vertical" : "cms",
        |    "dtName" : "sku.cat2",
        |    "time_spend" : 9527168,
        |    "component" : "uWidgetSKU",
        |    "configDtId" : 971
        |  },
        |  "params" : {
        |    "lowerLimit" : 1,
        |    "itemsOnPage" : 1,
        |    "offset" : "searchCPM",
        |    "title" : "Спонсорский товар",
        |    "providerAlgo" : "searchCPM"
        |  },
        |  "vertical" : "cms",
        |  "version" : 1,
        | "widgetToken" : ""
        |}
      """.stripMargin

    decode[Component](componentRawJson).isRight shouldBe true
  }
}
