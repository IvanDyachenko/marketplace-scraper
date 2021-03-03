package marketplace.models.yandex.market

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import marketplace.models.yandex.market.Model

class ModelSpec extends AnyFlatSpec with Matchers {
  it should "decode model identifier from a string" in {
    val rawJson = "13518985"

    decode[Model.ModelId](rawJson).isRight shouldBe true
  }

  it should "decode model identifier from a JSON object which contais 'id' field" in {
    val rawJson =
      """
        |{
        |  "id": 13518985
        |}
      """.stripMargin

    decode[Model.ModelId](rawJson).isRight shouldBe true
  }
}
