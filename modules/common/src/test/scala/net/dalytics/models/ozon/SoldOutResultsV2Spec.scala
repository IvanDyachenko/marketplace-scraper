package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class SoldOutResultsV2Spec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode SoldOutResultsV2.Success from a valid JSON (1) (circe)" in {
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

    val result = decode[SoldOutResultsV2.Success](soldOutResultsV2RawJson).value

    result should be(SoldOutResultsV2.Success(List.empty))
  }

  it should "decode SoldOutResultsV2.Success from a valid JSON (1) (tethys)" in {
    val soldOutResultsV2RawJson =
      """
        |{
        |  "soldOutResultsV2-189813-default-1": {
        |    "items": [],
        |    "templates": [
        |      {
        |        "name": "search",
        |        "value": [
        |          "action",
        |          "price",
        |          "title",
        |          "rating",
        |          "action"
        |        ],
        |        "imageRatio": "1:1",
        |        "tileSize": "default"
        |      }
        |    ],
        |    "page": 1,
        |    "cols": 12
        |  }
        |}
      """.stripMargin

    val component           = Component.SoldOutResultsV2("soldOutResultsV2-189813-default-1" @@ Component.StateId)
    implicit val jsonReader = SoldOutResultsV2.tethysJsonReader(component)
    val soldOutResultsV2    = soldOutResultsV2RawJson.jsonAs[SoldOutResultsV2].value

    soldOutResultsV2 should be(SoldOutResultsV2.Success(List.empty))
  }

  it should "decode SoldOutResultsV2.Success from a valid JSON (2) (circe)" in {
    val soldOutResultsV2RawJson =
      """
        |{
        |  "items": null,
        |  "templates": null,
        |  "page": 1,
        |  "cols": 12
        |}
      """.stripMargin

    val result = decode[SoldOutResultsV2.Success](soldOutResultsV2RawJson).value

    result should be(SoldOutResultsV2.Success(List.empty))
  }

  it should "decode SoldOutResultsV2.Success from a valid JSON (2) (tethys)" in {
    val soldOutResultsV2RawJson =
      """
        |{
        |  "soldOutResultsV2-189813-default-1": {
        |    "items": null,
        |    "templates": null,
        |    "page": 1,
        |    "cols": 12
        |  }
        |}
      """.stripMargin

    val component           = Component.SoldOutResultsV2("soldOutResultsV2-189813-default-1" @@ Component.StateId)
    implicit val jsonReader = SoldOutResultsV2.tethysJsonReader(component)
    val soldOutResultsV2    = soldOutResultsV2RawJson.jsonAs[SoldOutResultsV2].value

    soldOutResultsV2 should be(SoldOutResultsV2.Success(List.empty))
  }
}
