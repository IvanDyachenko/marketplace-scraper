package net.dalytics.models.wildberries

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class CatalogMenuSpec extends AnyFlatSpec with Matchers {

  it should "decode CatalogMenu from a valid JSON (1)" in {
    val catalogMenuRawJson =
      """
        |{
        |  "state": 0,
        |  "data": {
        |    "catalog": [
        |      {
        |        "id": 306,
        |        "name": "Женщинам",
        |        "pageUrl": "/catalog/zhenshchinam",
        |        "shardKey": "presets/women_clothes",
        |        "query": "preset=1001&sort=default",
        |        "filters": "dlvr;brand;price;color;wbsize;season;consists",
        |        "urlType": "catalog2",
        |        "childNodes": []
        |      },
        |      {
        |        "id": 566,
        |        "name": "Мужчинам",
        |        "pageUrl": "/catalog/muzhchinam",
        |        "shardKey": "men_clothes",
        |        "query": "kind=1&subject=1;4;152;1513",
        |        "filters": "dlvr;brand;price;color;wbsize;season;consists",
        |        "urlType": "catalog2",
        |        "childNodes": [
        |          {
        |            "id": 573,
        |            "name": "Одежда",
        |            "pageUrl": "/catalog/muzhchinam/odezhda",
        |            "shardKey": "men_clothes",
        |            "query": "kind=1;11&subject=1",
        |            "filters": "dlvr;brand;price;color;season;consists",
        |            "urlType": "catalog2",
        |            "childNodes": [
        |              {
        |                "id": 8144,
        |                "name": "Брюки",
        |                "pageUrl": "/catalog/muzhchinam/odezhda/bryuki-i-shorty",
        |                "shardKey": "men_clothes",
        |                "query": "kind=1;11&subject=11;147;216;2287;4575",
        |                "filters": "xsubject;dlvr;brand;price;color;wbsize;season;consists;f4;f1015;f85580;f23771;f6153",
        |                "urlType": "xfilter",
        |                "childNodes": []
        |              }
        |            ]
        |          }
        |        ]
        |      },
        |      {
        |        "id": 115,
        |        "name": "Детям",
        |        "pageUrl": "/catalog/detyam",
        |        "shardKey": "children_things",
        |        "query": "kind=3;5;6&subject=1;4;309;883;1513;2638;4607;4735;4776;4777;4778;4779;4781;4782;4783;4784;4785;4786;4787;5828",
        |        "filters": "dlvr;brand;price;color;wbsize;consists",
        |        "urlType": "catalog2",
        |        "childNodes": []
        |      }
        |    ]
        |  }
        |}
      """.stripMargin

    decode[CatalogMenu](catalogMenuRawJson).isRight shouldBe true
  }
}
