package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

import supertagged.postfix._

class CatalogSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode Catalog which contains 'categoryMenu' from a valid JSON (tethys)" in {
    val catalogRawJson =
      """
        |{
        |  "categoryMenu": {
        |    "categoryMenu-281296-default-1": {
        |      "categories": [
        |        {
        |          "id": 6500,
        |          "name": "Красота и здоровье",
        |          "isAdult": false,
        |          "link": "/category/krasota-i-zdorove-6500/",
        |          "deeplink": "ozon://category/krasota-i-zdorove-6500/",
        |          "isActive": false,
        |          "cellTrackingInfo": {
        |            "type": "category",
        |            "id": "6500",
        |            "index": 1,
        |            "title": "Красота и здоровье"
        |          },
        |          "categories": [
        |            {
        |              "id": 6637,
        |              "name": "Уход за телом",
        |              "isAdult": false,
        |              "link": "/category/uhod-za-telom-6637/",
        |              "deeplink": "ozon://category/uhod-za-telom-6637/",
        |              "isActive": false,
        |              "cellTrackingInfo": {
        |                "type": "category",
        |                "id": "6637",
        |                "index": 1,
        |                "title": "Уход за телом"
        |              },
        |              "categories": [
        |                {
        |                  "id": 31294,
        |                  "name": "Увлажнение и питание",
        |                  "isAdult": false,
        |                  "link": "/category/uvlazhnenie-i-pitanie-dlya-tela-31294/",
        |                  "deeplink": "ozon://category/uvlazhnenie-i-pitanie-dlya-tela-31294/",
        |                  "isActive": false,
        |                  "cellTrackingInfo": {
        |                    "type": "category",
        |                    "id": "31294",
        |                    "index": 1,
        |                    "title": "Увлажнение и питание для тела"
        |                  },
        |                  "categories": [
        |                    {
        |                      "id": 6563,
        |                      "name": "Концентраты и бальзамы",
        |                      "isAdult": false,
        |                      "link": "/category/kontsentraty-dlya-uhoda-za-kozhey-6563/",
        |                      "deeplink": "ozon://category/kontsentraty-dlya-uhoda-za-kozhey-6563/",
        |                      "isActive": true,
        |                      "cellTrackingInfo": {
        |                        "type": "category",
        |                        "id": "6563",
        |                        "index": 1,
        |                        "title": "Концентраты и бальзамы для тела"
        |                      },
        |                      "categories": [],
        |                      "redirectTo": ""
        |                    }
        |                  ],
        |                  "redirectTo": ""
        |                }
        |              ],
        |              "redirectTo": ""
        |            }
        |          ],
        |          "redirectTo": ""
        |        }
        |      ],
        |      "modalUrl": "/modal/categoryMenu"
        |    }
        |  },
        |  "shared": {
        |    "catalog": {
        |      "activeSort": "",
        |      "brand": null,
        |      "breadCrumbs": null,
        |      "category": {
        |        "id": 6563,
        |        "name": "Концентраты и бальзамы для тела",
        |        "imageUrls": {
        |          "catalog_logo": "https://cdn1.ozone.ru/multimedia/1021953986.jpg"
        |        },
        |        "isAdult": false,
        |        "catalogName": "Концентраты и бальзамы"
        |      },
        |      "categoryPredicted": false,
        |      "context": "category",
        |      "correctedText": "",
        |      "currentPage": 0,
        |      "currentText": "",
        |      "currentUrl": "/modal/categoryMenu/category/kontsentraty-dlya-uhoda-za-kozhey-6563/",
        |      "totalFound": 0,
        |      "totalPages": 0
        |    }
        |  }
        |}
      """.stripMargin

    val component           = Component.CategoryMenu("categoryMenu-281296-default-1" @@ Component.StateId)
    val layout              = Layout(List(component))
    implicit val jsonReader = Catalog.tethysJsonReader(layout)
    val catalog             = catalogRawJson.jsonAs[Catalog].value

    catalog.page should be(Page(0, 0, 0))
    catalog.category should be(
      Category(6563L @@ Category.Id, "Концентраты и бальзамы для тела" @@ Category.Name, "Концентраты и бальзамы" @@ Catalog.Name)
    )
    catalog.categoryMenu.value.categories.length should be(1)
  }

  it should "decode Catalog which contains 'searchResultsV2' from a valid JSON (tethys)" in {
    val catalogRawJson =
      """
        |{
        |  "searchResultsV2": {
        |    "searchResultsV2-189805-default-1": {
        |      "items": [],
        |      "templates": [
        |        {
        |          "name": "search",
        |          "value": [
        |            "action",
        |            "atom",
        |            "label",
        |            "title",
        |            "atom",
        |            "rating",
        |            "action",
        |            "textSmall"
        |          ],
        |          "imageRatio": "1:1.4",
        |          "tileSize": "default"
        |        }
        |      ],
        |      "page": 1,
        |      "cols": 12
        |    }
        |  },
        |  "shared": {
        |    "catalog": {
        |      "activeSort": "score",
        |      "brand": null,
        |      "breadCrumbs": null,
        |      "category": {
        |        "id": 7508,
        |        "name": "Футболки женские",
        |        "imageUrls": {
        |          "catalog_logo": "https://cdn1.ozone.ru/s3/multimedia-l/6010693713.jpg"
        |        },
        |        "isAdult": false,
        |        "catalogName": "Футболки"
        |      },
        |      "categoryPredicted": false,
        |      "context": "category",
        |      "correctedText": "",
        |      "currentPage": 1,
        |      "currentText": "",
        |      "currentUrl": "/category/futbolki-zhenskie-7508/",
        |      "totalFound": 55974,
        |      "totalPages": 1555
        |    }
        |  }
        |}
      """.stripMargin

    val component           = Component.SearchResultsV2("searchResultsV2-189805-default-1" @@ Component.StateId)
    val layout              = Layout(List(component))
    implicit val jsonReader = Catalog.tethysJsonReader(layout)
    val catalog             = catalogRawJson.jsonAs[Catalog].value

    catalog.page should be(Page(1, 1555, 55974))
    catalog.category should be(
      Category(7508L @@ Category.Id, "Футболки женские" @@ Category.Name, "Футболки" @@ Catalog.Name)
    )
    catalog.searchResultsV2.value should be(SearchResultsV2.Success(List.empty))
  }

  it should "decode Catalog which contains 'soldOutResultsV2' from a valid JSON (tethys)" in {
    val catalogRawJson =
      """
        |{
        |  "shared": {
        |    "catalog": {
        |      "activeSort": "score",
        |      "brand": null,
        |      "breadCrumbs": null,
        |      "category": {
        |        "id": 6563,
        |        "name": "Концентраты и бальзамы для тела",
        |        "imageUrls": {
        |          "catalog_logo": "https://cdn1.ozone.ru/multimedia/1021953986.jpg"
        |        },
        |        "isAdult": false,
        |        "catalogName": "Концентраты и бальзамы"
        |      },
        |      "categoryPredicted": false,
        |      "context": "category",
        |      "correctedText": "",
        |      "currentSoldOutPage": 1,
        |      "currentText": "",
        |      "currentUrl": "/category/kontsentraty-dlya-uhoda-za-kozhey-6563/bioderma-27367890/",
        |      "totalFound": 2,
        |      "totalPages": 1
        |    }
        |  },
        |  "soldOutHeader": {
        |    "soldOutHeader-189812-default-1": {
        |      "title": "Товары не в наличии",
        |      "subtitle": "Подпишитесь и мы сообщим о поступлении",
        |      "separatorHeight": 2
        |    }
        |  },
        |  "soldOutResultsV2": {
        |    "soldOutResultsV2-189813-default-1": {
        |      "items": [],
        |      "templates": [
        |        {
        |          "name": "search",
        |          "value": [
        |            "action",
        |            "price",
        |            "title",
        |            "rating",
        |            "action"
        |          ],
        |          "imageRatio": "1:1",
        |          "tileSize": "default"
        |        }
        |      ],
        |      "page": 1,
        |      "cols": 12
        |    }
        |  }
        |}
      """.stripMargin

    val component           = Component.SoldOutResultsV2("soldOutResultsV2-189813-default-1" @@ Component.StateId)
    val layout              = Layout(List(component))
    implicit val jsonReader = Catalog.tethysJsonReader(layout)
    val catalog             = catalogRawJson.jsonAs[Catalog].value

    catalog.page should be(Page(1, 1, 2))
    catalog.category should be(
      Category(6563L @@ Category.Id, "Концентраты и бальзамы для тела" @@ Category.Name, "Концентраты и бальзамы" @@ Catalog.Name)
    )
    catalog.soldOutResultsV2.value should be(SoldOutResultsV2.Success(List.empty))
  }
}
