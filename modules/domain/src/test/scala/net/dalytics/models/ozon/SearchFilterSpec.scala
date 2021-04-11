package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import supertagged.postfix._

class SearchFilterSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode BrandFilter from a valid JSON" in {
    val brandFilterRawJson =
      """
        |{
        |  "key": 139867648,
        |  "value": "Maunfeld"
        |}
      """.stripMargin

    val decodedBrandFilter = decode[BrandFilter](brandFilterRawJson)

    decodedBrandFilter.value should be(BrandFilter(139867648L @@ Brand.Id))
  }

  it should "decode BrandFilters from a valid JSON" in {
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

    val decodedSearchFilters = decode[SearchFilters](searchFiltersRawJson)

    decodedSearchFilters.value should be(
      BrandFilters(List(BrandFilter(139867648L @@ Brand.Id), BrandFilter(7577796L @@ Brand.Id)))
    )
  }
}
