package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

import supertagged.postfix._

class PriceSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Price from a valid JSON (tethys)" in {
    val priceRawJson =
      """
        |{
        |  "price": 1600,
        |  "finalPrice": 999,
        |  "discount": 37
        |}
      """.stripMargin

    val price = priceRawJson.jsonAs[Price].value

    price should be(Price(1600d @@ Price.Value, 999d @@ Price.Value, (37: Byte) @@ Price.Percent))
  }
}
