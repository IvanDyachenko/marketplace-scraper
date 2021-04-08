package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import supertagged.postfix._

class SearchFilterValuesSpec extends AnyFlatSpec with Matchers with EitherValues {
  it should "decode SearchFilterBrands from a valid JSON" in {
    val searchFilterBrandsRawJson =
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

    val decodedSearchFilterBrands = decode[SearchFilterValues](searchFilterBrandsRawJson)

    decodedSearchFilterBrands.value should be(
      SearchFilterBrands(List(SearchFilterBrand(139867648L @@ Brand.Id), SearchFilterBrand(7577796L @@ Brand.Id)))
    )
  }
}
