package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import supertagged.postfix._

class SearchFilterValueSpec extends AnyFlatSpec with Matchers with EitherValues {
  it should "decode SearchFilterBrand from a valid JSON" in {
    val searchFilterBrandRawJson =
      """
        |{
        |  "key": 139867648,
        |  "value": "Maunfeld"
        |}
      """.stripMargin

    val decodedSearchFilterBrand = decode[SearchFilterValue](searchFilterBrandRawJson)

    decodedSearchFilterBrand.value should be(SearchFilterBrand(139867648L @@ Brand.Id))
  }
}
