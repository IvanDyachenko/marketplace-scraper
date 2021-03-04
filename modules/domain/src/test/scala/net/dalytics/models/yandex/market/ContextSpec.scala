package net.dalytics.models.yandex.market

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import net.dalytics.models.yandex.market.Context

class ContextSpec extends AnyFlatSpec with Matchers {
  it should "decode context from a valid JSON" in {
    val contextRawJson =
      """
        |{
        |  "region" : {
        |    "childCount" : 14,
        |    "name" : "Москва",
        |    "country" : {
        |       "id" : 225,
        |       "type" : "COUNTRY",
        |       "name" : "Россия",
        |       "childCount" : 10
        |    },
        |    "type" : "CITY",
        |    "id" : 213
        |  },
        |  "processingOptions" : {
        |    "adult" : false,
        |    "checkSpelled" : true,
        |    "restrictionAge18" : false,
        |    "highlightedText" : ""
        |  },
        |  "currency" : {
        |    "name" : "руб.",
        |    "id" : "RUR"
        |  },
        |  "time" : "2020-10-27T15:35:07.781+03:00",
        |  "id" : "1603802106944/382667bd126e6adc98dadf46a6b20500",
        |  "page" : {
        |    "total" : 73,
        |    "totalItems" : 1741,
        |    "number" : 4,
        |    "count" : 24
        |  }
        |}
      """.stripMargin

    decode[Context](contextRawJson).isRight shouldBe true
  }
}
