package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

class ButtonSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Button.AddToCartWithQuantity from a valid JSON (circe)" in {
    val buttonRawJson =
      """
        |{
        |  "isActive": false,
        |  "type": "addToCartButtonWithQuantity",
        |  "default": {
        |    "addToCartButtonWithQuantity": {
        |      "maxItems": 700,
        |      "currentItems": 0,
        |      "text": "В корзину",
        |      "style": "STYLE_TYPE_PRIMARY",
        |      "action": {
        |        "quantity": 1,
        |        "id": "215730070"
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    val button = decode[Button](buttonRawJson).value

    button should be(Button.AddToCartWithQuantity(Button.AddToCartWithQuantity.Action(1), 700))
  }

  it should "decode Button.AddToCartWithQuantity from a valid JSON (tethys)" in {
    val buttonRawJson =
      """
        |{
        |  "isActive": false,
        |  "type": "addToCartButtonWithQuantity",
        |  "default": {
        |    "addToCartButtonWithQuantity": {
        |      "maxItems": 700,
        |      "currentItems": 0,
        |      "text": "В корзину",
        |      "style": "STYLE_TYPE_PRIMARY",
        |      "action": {
        |        "quantity": 1,
        |        "id": "215730070"
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    val button = buttonRawJson.jsonAs[Button].value

    button should be(Button.AddToCartWithQuantity(Button.AddToCartWithQuantity.Action(1), 700))
  }
}
