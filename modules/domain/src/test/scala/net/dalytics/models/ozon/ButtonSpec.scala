package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

class ButtonSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode Button.AddToCartWithQuantity from a valid JSON (circe) (1)" in {
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

    button should be(Button.AddToCartWithQuantity(1, 700))
  }

  it should "decode Button.AddToCartWithQuantity from a valid JSON (circe) (2)" in {
    val buttonRawJson =
      """
        |{
        |  "isActive": false,
        |  "default": {
        |    "type": "addToCartButtonWithQuantity",
        |    "addToCartButtonWithQuantity": {
        |      "text": "В корзину",
        |      "style": "STYLE_TYPE_PRIMARY",
        |      "maxItems": 3,
        |      "currentItems": 0,
        |      "action": {
        |        "id": "200352826",
        |        "quantity": 1
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    val button = decode[Button](buttonRawJson).value

    button should be(Button.AddToCartWithQuantity(1, 3))
  }

  it should "decode Button.AddToCartWithQuantity from a valid JSON (tethys) (1)" in {
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

    val button = buttonRawJson.jsonAs[Option[Button]].value.value

    button should be(Button.AddToCartWithQuantity(1, 700))
  }

  it should "decode Button.AddToCartWithQuantity from a valid JSON (tethys) (2)" in {
    val buttonRawJson =
      """
        |{
        |  "isActive": false,
        |  "default": {
        |    "type": "addToCartButtonWithQuantity",
        |    "addToCartButtonWithQuantity": {
        |      "text": "В корзину",
        |      "style": "STYLE_TYPE_PRIMARY",
        |      "maxItems": 3,
        |      "currentItems": 0,
        |      "action": {
        |        "id": "200352826",
        |        "quantity": 1
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    val button = buttonRawJson.jsonAs[Option[Button]].value.value

    button should be(Button.AddToCartWithQuantity(1, 3))
  }
}
