package net.dalytics.models.wildberries

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class CatalogSpec extends AnyFlatSpec with Matchers {

  it should "decode Catalog from a valid JSON (1)" in {
    val catalogRawJson =
      """
        |{
        |  "id": 306,
        |  "name": "Женщинам",
        |  "pageUrl": "/catalog/zhenshchinam",
        |  "shardKey": "presets/women_clothes",
        |  "query": "preset=1001&sort=default",
        |  "filters": "dlvr;brand;price;color;wbsize;season;consists",
        |  "urlType": "catalog2",
        |  "childNodes": [
        |    {
        |      "id": 366,
        |      "name": "Одежда",
        |      "pageUrl": "/catalog/zhenshchinam/odezhda",
        |      "shardKey": "presets/women_clothes",
        |      "query": "preset=1001&sort=default",
        |      "filters": "dlvr;brand;price;wbsize",
        |      "urlType": "catalog2",
        |      "childNodes": [
        |        {
        |          "id": 8126,
        |          "name": "Блузки и рубашки",
        |          "pageUrl": "/catalog/zhenshchinam/odezhda/bluzki-i-rubashki",
        |          "shardKey": "bl_shirts",
        |          "query": "kind=2&subject=41;184;1429",
        |          "filters": "xsubject;dlvr;brand;price;color;wbsize;consists;f4;f6;f50;f10",
        |          "urlType": "xfilter",
        |          "childNodes": []
        |        },
        |        {
        |          "id": 8129,
        |          "name": "Водолазки",
        |          "pageUrl": "/catalog/zhenshchinam/odezhda/vodolazki",
        |          "shardKey": "rollnecks",
        |          "query": "kind=2&subject=153",
        |          "filters": "dlvr;brand;price;color;wbsize;season;consists;f10",
        |          "urlType": "catalog2"
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin

    decode[Catalog](catalogRawJson).isRight shouldBe true
  }

  it should "decode Catalog from a valid JSON (2)" in {
    val catalogRawJson =
      """
        |{
        |  "id": 4853,
        |  "name": "Бренды  ",
        |  "pageUrl": "/brandlist/all",
        |  "urlType": "brandList",
        |  "childNodes": [
        |    {
        |      "id": 7987,
        |      "name": "Бренды на букву",
        |      "pageUrl": "/brandlist/all",
        |      "urlType": "brandList"
        |    },
        |    {
        |      "id": 7988,
        |      "name": "Страницы брендов",
        |      "pageUrl": "/brandlist/all",
        |      "urlType": "brandList",
        |      "childNodes": [
        |        {
        |          "id": 7995,
        |          "name": "ASICS",
        |          "pageUrl": "/brands/asics",
        |          "urlType": "catalog"
        |        }
        |      ]
        |    }
        |  ]
        |}
      """.stripMargin

    decode[Catalog](catalogRawJson).isRight shouldBe true
  }

  it should "decode Catalog from a valid JSON (3)" in {
    val catalogRawJson =
      """
        |{
        |  "id": 62813,
        |  "name": "Цифровые товары",
        |  "pageUrl": "https://www.wildberries.ru/services/tsifrovye-tovary",
        |  "urlType": "external"
        |}
      """.stripMargin

    decode[Catalog](catalogRawJson).isRight shouldBe true
  }
}
