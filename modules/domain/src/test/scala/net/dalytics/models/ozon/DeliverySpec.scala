package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

class DeliverySpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Delivery from a valid JSON (1) (circe)" in {
    val deliveryRawJson =
      """
        |{
        |  "deliverySchema": "Retail",
        |  "deliveryTimeDiffDays": null
        |}
      """.stripMargin

    val delivery = decode[Delivery](deliveryRawJson).value

    delivery should be(Delivery(Delivery.Schema.Retail, 0: Short))
  }

  it should "decode Delivery from a valid JSON (1) (tethys)" in {
    val deliveryRawJson =
      """
        |{
        |  "deliverySchema": "Retail",
        |  "deliveryTimeDiffDays": null
        |}
      """.stripMargin

    val delivery = deliveryRawJson.jsonAs[Delivery].value

    delivery should be(Delivery(Delivery.Schema.Retail, 0: Short))
  }

  it should "decode Delivery from a valid JSON (2) (circe)" in {
    val deliveryRawJson =
      """
        |{
        |  "deliverySchema": "FBO",
        |  "deliveryTimeDiffDays": 42
        |}
      """.stripMargin

    val delivery = decode[Delivery](deliveryRawJson).value

    delivery should be(Delivery(Delivery.Schema.FBO, 42: Short))
  }

  it should "decode Delivery from a valid JSON (2) (tethys)" in {
    val deliveryRawJson =
      """
        |{
        |  "deliverySchema": "FBO",
        |  "deliveryTimeDiffDays": 42
        |}
      """.stripMargin

    val delivery = deliveryRawJson.jsonAs[Delivery].value

    delivery should be(Delivery(Delivery.Schema.FBO, 42: Short))
  }
}
