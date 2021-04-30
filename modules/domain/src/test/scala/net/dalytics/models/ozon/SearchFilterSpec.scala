package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class SearchFilterSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode BrandFilter from a valid JSON (circe)" in {
    val brandFilterRawJson =
      """
        |{
        |  "key": 139867648,
        |  "value": "Maunfeld"
        |}
      """.stripMargin

    val brandFilter = decode[BrandFilter](brandFilterRawJson).value

    brandFilter should be(BrandFilter(139867648L @@ Brand.Id))
  }

  it should "decode BrandFilter from a valid JSON (tethys)" in {
    val brandFilterRawJson =
      """
        |{
        |  "key": 139867648,
        |  "value": "Maunfeld"
        |}
      """.stripMargin

    val brandFilter = brandFilterRawJson.jsonAs[BrandFilter].value

    brandFilter should be(BrandFilter(139867648L @@ Brand.Id))
  }

  it should "decode BrandFilters from a valid JSON (circe)" in {
    val searchFiltersRawJson =
      """
        |{
        |  "key": "brand",
        |  "name": "Бренды",
        |  "type": "RESPONSE_FILTER_TYPE_MULTI",
        |  "values": [
        |    {
        |      "key": 139867648,
        |      "isActive": false,
        |      "isDisabled": false,
        |      "urlValue": "139867648",
        |      "value": "Maunfeld",
        |      "count": 0,
        |      "count2": null,
        |      "icon": "",
        |      "iconTintColor": ""
        |    },
        |    {
        |      "key": 7577796,
        |      "isActive": false,
        |      "isDisabled": false,
        |      "urlValue": "7577796",
        |      "value": "Bosch",
        |      "count": 0,
        |      "count2": null,
        |      "icon": "",
        |      "iconTintColor": ""
        |    }
        |  ]
        |}
      """.stripMargin

    val searchFilters = decode[SearchFilters](searchFiltersRawJson).value

    searchFilters should be(BrandFilters(List(BrandFilter(139867648L @@ Brand.Id), BrandFilter(7577796L @@ Brand.Id))))
  }

  it should "decode BrandFilters from a valid JSON (tethys)" in {
    val searchFiltersRawJson =
      """
        |{
        |  "key": "brand",
        |  "name": "Бренды",
        |  "type": "RESPONSE_FILTER_TYPE_MULTI",
        |  "values": [
        |    {
        |      "key": 139867648,
        |      "isActive": false,
        |      "isDisabled": false,
        |      "urlValue": "139867648",
        |      "value": "Maunfeld",
        |      "count": 0,
        |      "count2": null,
        |      "icon": "",
        |      "iconTintColor": ""
        |    },
        |    {
        |      "key": 7577796,
        |      "isActive": false,
        |      "isDisabled": false,
        |      "urlValue": "7577796",
        |      "value": "Bosch",
        |      "count": 0,
        |      "count2": null,
        |      "icon": "",
        |      "iconTintColor": ""
        |    }
        |  ]
        |}
      """.stripMargin

    val searchFilters = searchFiltersRawJson.jsonAs[SearchFilters].value

    searchFilters should be(BrandFilters(List(BrandFilter(139867648L @@ Brand.Id), BrandFilter(7577796L @@ Brand.Id))))
  }
}
