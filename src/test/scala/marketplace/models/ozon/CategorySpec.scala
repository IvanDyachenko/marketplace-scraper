package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class CategorySpec extends AnyFlatSpec with Matchers {

  it should "decode Category from a valid JSON (1)" in {
    val categoryRawJson =
      """
        |{
        |  "id": 14501,
        |  "name": "Посуда",
        |  "isAdult": false,
        |  "link": "/category/posuda-i-kuhonnye-prinadlezhnosti-14501/",
        |  "deeplink": "ozon://category/posuda-i-kuhonnye-prinadlezhnosti-14501/",
        |  "isActive": false,
        |  "cellTrackingInfo": {
        |    "type": "category",
        |    "id": "14501",
        |    "index": 1,
        |    "title": "Посуда"
        |  },
        |  "categories": [],
        |  "redirectTo": ""
        |}
      """.stripMargin

    decode[Category](categoryRawJson).isRight shouldBe true
  }

  it should "decode Category from a valid JSON (2)" in {
    val categoryRawJson =
      """
        |{
        |  "id": 12348,
        |  "catalogName": "Корма и лакомства",
        |  "name": "Корм для кошек",
        |  "isAdult": false,
        |  "imageUrls": {
        |    "catalog_logo": "https://cdn1.ozone.ru/s3/multimedia-y/6004911982.jpg"
        |  }
        |}
      """.stripMargin

    decode[Category](categoryRawJson).isRight shouldBe true
  }
}
