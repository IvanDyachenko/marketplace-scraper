package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class BrandSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Brand from a valid JSON (circe)" in {
    val brandRawJson =
      """
        |{
        |  "brandId": 135450384,
        |  "brand": "Gehwol"
        |}
      """.stripMargin

    val brand = decode[Brand](brandRawJson).value

    brand should be(Brand(135450384L @@ Brand.Id, "Gehwol" @@ Brand.Name))
  }

  it should "decode Brand from a valid JSON (tethys)" in {
    val brandRawJson =
      """
        |{
        |  "brandId": 135450384,
        |  "brand": "Gehwol"
        |}
      """.stripMargin

    val brand = brandRawJson.jsonAs[Brand].value

    brand should be(Brand(135450384L @@ Brand.Id, "Gehwol" @@ Brand.Name))
  }
}
