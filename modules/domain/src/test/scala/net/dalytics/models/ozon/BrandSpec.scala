package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import supertagged.postfix._

class BrandSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Brand from a valid JSON (1)" in {
    val brandRawJson =
      """
        |{
        |  "brandId": 135450384,
        |  "brand": "Gehwol"
        |}
      """.stripMargin

    val decodedBrand = decode[Brand](brandRawJson)

    decodedBrand.value should be(Brand(135450384L @@ Brand.Id, "Gehwol" @@ Brand.Name))
  }

  it should "decode Brand from a valid JSON (2)" in {
    val brandRawJson =
      """
        |{
        |  "key": 139867648,
        |  "value": "Maunfeld"
        |}
      """.stripMargin

    val decodedBrand = decode[Brand](brandRawJson)

    decodedBrand.value should be(Brand(139867648L @@ Brand.Id, "Maunfeld" @@ Brand.Name))
  }
}
