package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class DeliverySpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Delivery from a valid JSON (1)" in {
    val deliveryRawJson =
      """
        |{
        |  "deliverySchema": "Retail",
        |  "deliveryTimeDiffDays": null
        |}
      """.stripMargin

    val decodedDelivery = decode[Delivery](deliveryRawJson)

    decodedDelivery.value should be(Delivery(Delivery.Schema.Retail, 0))
  }

  it should "decode Delivery from a valid JSON (2)" in {
    val deliveryRawJson =
      """
        |{
        |  "deliverySchema": "FBO",
        |  "deliveryTimeDiffDays": 42
        |}
      """.stripMargin

    val decodedDelivery = decode[Delivery](deliveryRawJson)

    decodedDelivery.value should be(Delivery(Delivery.Schema.FBO, 42))
  }
}
