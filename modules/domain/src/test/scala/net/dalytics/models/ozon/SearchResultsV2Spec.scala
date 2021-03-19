package net.dalytics.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class SearchResultsV2Spec extends AnyFlatSpec with Matchers {

  ignore should "decode SearchResultsV2.Success from a valid JSON" in {
    val searchResultsV2RawJson =
      """
        |{
        |  "cols": 12,
        |  "templates": [
        |    {
        |      "name": "search",
        |      "value": [
        |        "action",
        |        "price",
        |        "label",
        |        "textSmall",
        |        "title",
        |        "action",
        |        "textSmall"
        |      ],
        |      "imageRatio": "1:1",
        |      "tileSize": "default"
        |    }
        |  ],
        |  "page": 250,
        |  "items": []
        |}
      """.stripMargin

    decode[CategorySearchResultsV2](searchResultsV2RawJson).isRight shouldBe true
    //decode[SearchResultsV2.Success](searchResultsV2RawJson).isRight shouldBe true
  }

  ignore should "decode SearchResultsV2.Failure from a valid JSON" in {
    val failureSearchResultsV2RawJson =
      """
        |{
        |  "error" : "internal server error"
        |}
      """.stripMargin

    decode[CategorySearchResultsV2](failureSearchResultsV2RawJson).isRight shouldBe true
    decode[CategorySearchResultsV2.Failure](failureSearchResultsV2RawJson).isRight shouldBe true
  }
}
