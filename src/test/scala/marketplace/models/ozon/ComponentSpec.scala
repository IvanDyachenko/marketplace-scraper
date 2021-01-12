package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class ComponentSpec extends AnyFlatSpec with Matchers {
  it should "decode SearchResultsV2-component from a valid JSON" in {
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

    decode[Component](componentRawJson).isRight shouldBe true
  }

  it should "decode UWidgetSKU-component from a valid JSON" in {
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
