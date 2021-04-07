package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class SoldOutResultsV2Spec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode SoldOutResultsV2.Success (1) from a valid JSON" in {
    val soldOutResultsV2RawJson =
      """
        |{
        |  "items": [],
        |  "templates": [
        |    {
        |      "name": "search",
        |      "value": [
        |        "action",
        |        "price",
        |        "title",
        |        "rating",
        |        "action"
        |      ],
        |      "imageRatio": "1:1",
        |      "tileSize": "default"
        |    }
        |  ],
        |  "page": 1,
        |  "cols": 12
        |}
      """.stripMargin

    val decodedResult = decode[SoldOutResultsV2.Success](soldOutResultsV2RawJson)

    decodedResult.isRight shouldBe true
  }

  it should "decode SoldOutResultsV2.Success (2) from a valid JSON" in {
    val soldOutResultsV2RawJson =
      """
        |{
        |  "items": null,
        |  "templates": null,
        |  "page": 1,
        |  "cols": 12
        |}
      """.stripMargin

    val decodedResult = decode[SoldOutResultsV2.Success](soldOutResultsV2RawJson)

    decodedResult.isRight shouldBe true
  }
}
