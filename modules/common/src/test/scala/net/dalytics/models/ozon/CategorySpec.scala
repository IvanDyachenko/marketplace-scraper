package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class CategorySpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Category from a valid JSON (1) (circe)" in {
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

    val category = decode[Category](categoryRawJson).value

    category should be(Category(14501L @@ Category.Id, "Посуда" @@ Category.Name, Some(List.empty[Category])))
  }

  it should "decode Category from a valid JSON (1) (tethys)" in {
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

    val category = categoryRawJson.jsonAs[Category].value

    category should be(Category(14501L @@ Category.Id, "Посуда" @@ Category.Name, Some(List.empty[Category])))
  }

  it should "decode Category from a valid JSON (2) (circe)" in {
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

    val category = decode[Category](categoryRawJson).value

    category should be(Category(12348L @@ Category.Id, "Корм для кошек" @@ Category.Name, "Корма и лакомства" @@ Catalog.Name))
  }

  it should "decode Category from a valid JSON (2) (tethys)" in {
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

    val category = categoryRawJson.jsonAs[Category].value

    category should be(Category(12348L @@ Category.Id, "Корм для кошек" @@ Category.Name, "Корма и лакомства" @@ Catalog.Name))
  }

  it should "decode Category from a valid JSON (3) (tethys)" in {
    val categoryRawJson =
      """
        |{
        |  "id": 6500,
        |  "name": "Красота и здоровье",
        |  "isAdult": false,
        |  "link": "/category/krasota-i-zdorove-6500/",
        |  "deeplink": "ozon://category/krasota-i-zdorove-6500/",
        |  "isActive": false,
        |  "cellTrackingInfo": {
        |    "type": "category",
        |    "id": "6500",
        |    "index": 1,
        |    "title": "Красота и здоровье"
        |  },
        |  "categories": [
        |    {
        |      "id": 6637,
        |      "name": "Уход за телом",
        |      "isAdult": false,
        |      "link": "/category/uhod-za-telom-6637/",
        |      "deeplink": "ozon://category/uhod-za-telom-6637/",
        |      "isActive": false,
        |      "cellTrackingInfo": {
        |        "type": "category",
        |        "id": "6637",
        |        "index": 1,
        |        "title": "Уход за телом"
        |      },
        |      "categories": [
        |        {
        |          "id": 31294,
        |          "name": "Увлажнение и питание",
        |          "isAdult": false,
        |          "link": "/category/uvlazhnenie-i-pitanie-dlya-tela-31294/",
        |          "deeplink": "ozon://category/uvlazhnenie-i-pitanie-dlya-tela-31294/",
        |          "isActive": false,
        |          "cellTrackingInfo": {
        |            "type": "category",
        |            "id": "31294",
        |            "index": 1,
        |            "title": "Увлажнение и питание для тела"
        |          }
        |        }
        |      ]
        |    },
        |    {
        |      "id": 6563,
        |      "name": "Концентраты и бальзамы",
        |      "isAdult": false,
        |      "link": "/category/kontsentraty-dlya-uhoda-za-kozhey-6563/",
        |      "deeplink": "ozon://category/kontsentraty-dlya-uhoda-za-kozhey-6563/",
        |      "isActive": true,
        |      "cellTrackingInfo": {
        |        "type": "category",
        |        "id": "6563",
        |        "index": 1,
        |        "title": "Концентраты и бальзамы для тела"
        |      },
        |      "categories": [],
        |      "redirectTo": ""
        |    }
        |  ]
        |}
      """.stripMargin

    val category = categoryRawJson.jsonAs[Category].value

    val category31294 = Category(31294L @@ Category.Id, "Увлажнение и питание" @@ Category.Name)
    val category6637  = Category(6637L @@ Category.Id, "Уход за телом" @@ Category.Name, Some(List(category31294)))
    val category6563  = Category(6563L @@ Category.Id, "Концентраты и бальзамы" @@ Category.Name, Some(List.empty))
    val category6500  = Category(6500L @@ Category.Id, "Красота и здоровье" @@ Category.Name, Some(List(category6637, category6563)))

    category should be(category6500)
  }
}
