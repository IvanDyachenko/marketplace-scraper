package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

import supertagged.postfix._

class ResultSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode Result which contains 'categoryMenu' from a valid JSON (tethys)" in {
    val resultRawJson =
      """
        |{
        |  "cms": {
        |    "sellerList": {
        |      "sellerList-438294-default-15": {
        |        "title": "Все магазины",
        |        "items": [],
        |        "view": "list",
        |        "showAllLink": "/seller",
        |        "showAllDeepLink": "ozon://seller",
        |        "trackingInfo": {
        |          "click": {
        |            "actionType": "click",
        |            "key": "a-8d72c558b4646f1467cd8215a899cfecd4a9a0fc"
        |          },
        |          "view": {
        |            "actionType": "view",
        |            "key": "a-8d72c558b4646f1467cd8215a899cfecd4a9a0fc"
        |          }
        |        }
        |      }
        |    }
        |  },
        |  "layout": [
        |    {
        |      "vertical": "cms",
        |      "component": "sellerList",
        |      "stateId": "sellerList-438294-default-15",
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "marketing.sellerList",
        |        "vertical": "cms",
        |        "component": "sellerList",
        |        "version": 1,
        |        "originName": "marketing.sellerList",
        |        "originVertical": "marketing",
        |        "originComponent": "sellerList",
        |        "originVersion": 1,
        |        "id": 438294,
        |        "revisionId": 480225,
        |        "index": 1,
        |        "timeSpent": 9891874
        |      },
        |      "widgetToken": "w-5a485f304a9c7ba262e775f5694af8fdacf2d8db",
        |      "trackingOn": true
        |    }
        |  ],
        |  "layoutTrackingInfo": {
        |    "deviceType": "app",
        |    "layoutContainer": "default",
        |    "layoutId": 2900,
        |    "layoutPageIndex": 15,
        |    "layoutVersion": 7,
        |    "pageType": "entry_point_seller",
        |    "platform": "ios",
        |    "ruleId": 2744,
        |    "templateType": "mobile"
        |  },
        |  "nextPage": "/seller?last_id=148971&layout_container=default&layout_page_index=16&page=15",
        |  "pageInfo": {
        |    "url": "/seller?layout_container=default&layout_page_index=15&page=15",
        |    "pageType": "entry_point_seller",
        |    "context": "ozon",
        |    "ruleId": 2744,
        |    "layoutId": 2900,
        |    "layoutVersion": 7
        |  },
        |  "pageToken": "p-0a60681d7f0959c145d1ce6650c028c73265f29d",
        |  "prevPage": "/seller?last_id=0&layout_container=default&layout_page_index=14&page=15",
        |  "shared": {
        |    "context": "ozon",
        |    "layoutId": 2900,
        |    "layoutVersion": 7,
        |    "pageType": "entry_point_seller",
        |    "ruleId": 2744
        |  },
        |  "trackingPayloads": {}
        |}
      """.stripMargin

    val result = resultRawJson.jsonAs[Result].value

    val sellerList = SellerList.Success(List.empty)
    val cms        = Cms(Some(sellerList))

    result should be(Result(Some(cms), None))
  }

  it should "decode Result which contains 'searchResultsV2' from a valid JSON (tethys)" in {
    val resultRawJson =
      """
        |{
        |  "catalog": {
        |    "searchResultsV2": {
        |      "searchResultsV2-189805-default-1": {
        |        "items": [],
        |        "templates": [
        |          {
        |            "name": "search",
        |            "value": [
        |              "action",
        |              "atom",
        |              "label",
        |              "title",
        |              "atom",
        |              "rating",
        |              "action",
        |              "textSmall"
        |            ],
        |            "imageRatio": "1:1.4",
        |            "tileSize": "default"
        |          }
        |        ],
        |        "page": 1,
        |        "cols": 12
        |      }
        |    },
        |    "shared": {
        |      "catalog": {
        |        "activeSort": "score",
        |        "brand": null,
        |        "breadCrumbs": null,
        |        "category": {
        |          "id": 7508,
        |          "name": "Футболки женские",
        |          "imageUrls": {
        |            "catalog_logo": "https://cdn1.ozone.ru/s3/multimedia-l/6010693713.jpg"
        |          },
        |          "isAdult": false,
        |          "catalogName": "Футболки"
        |        },
        |        "categoryPredicted": false,
        |        "context": "category",
        |        "correctedText": "",
        |        "currentPage": 1,
        |        "currentText": "",
        |        "currentUrl": "/category/futbolki-zhenskie-7508/",
        |        "totalFound": 55974,
        |        "totalPages": 1555
        |      }
        |    }
        |  },
        |  "layout": [
        |    {
        |      "vertical": "products",
        |      "component": "skuGrid3",
        |      "stateId": "skuGrid3-519256-default-1",
        |      "params": {
        |        "brandShelfPlacement": "1"
        |      },
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "advBrandShelf",
        |        "vertical": "products",
        |        "component": "skuGrid3",
        |        "version": 1,
        |        "originName": "rtb.advBrandShelf",
        |        "originVertical": "rtb",
        |        "originComponent": "advBrandShelf",
        |        "originVersion": 1,
        |        "id": 519256,
        |        "configId": 5667,
        |        "configDtId": 15309,
        |        "revisionId": 721985,
        |        "index": 2,
        |        "dtName": "sku.grid3",
        |        "timeSpent": 85174952
        |      },
        |      "widgetToken": "b1a8b5a902f2a3ce5db0dada06d109c2f8ffddcf",
        |      "trackingOn": true
        |    },
        |    {
        |      "vertical": "catalog",
        |      "component": "searchResultsV2",
        |      "stateId": "searchResultsV2-189805-default-1",
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "catalog.searchResultsV2",
        |        "vertical": "catalog",
        |        "component": "searchResultsV2",
        |        "version": 1,
        |        "id": 189805,
        |        "revisionId": 113249,
        |        "index": 3,
        |        "timeSpent": 802967036
        |      },
        |      "widgetToken": "c9fab840aaca717def407dfd0ec4620a1b2c4dbc"
        |    }
        |  ],
        |  "layoutTrackingInfo": {
        |    "categoryId": 7508,
        |    "deviceType": "app",
        |    "layoutContainer": "default",
        |    "layoutId": 1571,
        |    "layoutPageIndex": 1,
        |    "layoutVersion": 65,
        |    "pageType": "category",
        |    "platform": "ios",
        |    "ruleId": 1389,
        |    "templateType": "mobile"
        |  },
        |  "nextPage": "/category/7508?layout_container=default&layout_page_index=2&page=2",
        |  "pageInfo": {
        |    "url": "/category/7508?layout_container=default&layout_page_index=1&page=1",
        |    "pageType": "category",
        |    "context": "ozon",
        |    "ruleId": 1389,
        |    "layoutId": 1571,
        |    "layoutVersion": 65
        |  },
        |  "pageToken": "d014486654af9c4e55d62ed52a22f0bc4a5e95de",
        |  "products": {
        |    "skuGrid3": {
        |      "skuGrid3-519256-default-1": {}
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
        |    },
        |    "context": "ozon",
        |    "layoutId": 1571,
        |    "layoutVersion": 65,
        |    "pageType": "category",
        |    "ruleId": 1389
        |  },
        |  "trackingPayloads": {}
        |}
      """.stripMargin

    val result = resultRawJson.jsonAs[Result].value

    val page            = Page(1, 1555, 55974)
    val category        = Category(7508L @@ Category.Id, "Футболки женские" @@ Category.Name, "Футболки" @@ Catalog.Name)
    val searchResultsV2 = SearchResultsV2.Success(List.empty)
    val catalog         = Catalog(page, category, None, Some(searchResultsV2), None)

    result should be(Result(None, Some(catalog)))
  }

  it should "decode Result which contains 'soldOutResultsV2' from a valid JSON (tethys)" in {
    val resultRawJson =
      """
        |{
        |  "catalog": {
        |    "shared": {
        |      "catalog": {
        |        "activeSort": "score",
        |        "brand": null,
        |        "breadCrumbs": null,
        |        "category": {
        |          "id": 6563,
        |          "name": "Концентраты и бальзамы для тела",
        |          "imageUrls": {
        |            "catalog_logo": "https://cdn1.ozone.ru/multimedia/1021953986.jpg"
        |          },
        |          "isAdult": false,
        |          "catalogName": "Концентраты и бальзамы"
        |        },
        |        "categoryPredicted": false,
        |        "context": "category",
        |        "correctedText": "",
        |        "currentSoldOutPage": 1,
        |        "currentText": "",
        |        "currentUrl": "/category/kontsentraty-dlya-uhoda-za-kozhey-6563/bioderma-27367890/",
        |        "totalFound": 1,
        |        "totalPages": 1
        |      }
        |    },
        |    "soldOutHeader": {
        |      "soldOutHeader-189812-default-1": {
        |        "title": "Товары не в наличии",
        |        "subtitle": "Подпишитесь и мы сообщим о поступлении",
        |        "separatorHeight": 2
        |      }
        |    },
        |    "soldOutResultsV2": {
        |      "soldOutResultsV2-189813-default-1": {
        |        "items": [],
        |        "templates": [
        |          {
        |            "name": "search",
        |            "value": [
        |              "action",
        |              "price",
        |              "title",
        |              "rating",
        |              "action"
        |            ],
        |            "imageRatio": "1:1",
        |            "tileSize": "default"
        |          }
        |        ],
        |        "page": 1,
        |        "cols": 12
        |      }
        |    }
        |  },
        |  "layout": [
        |    {
        |      "vertical": "products",
        |      "component": "skuFeedList",
        |      "stateId": "skuFeedList-519255-default-1",
        |      "params": {
        |        "itemsOnPage": 1,
        |        "lowerLimit": 1,
        |        "offset": "searchCPM",
        |        "providerAlgo": "searchCPM",
        |        "title": "Спонсорский товар"
        |      },
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "advProductShelf",
        |        "vertical": "products",
        |        "component": "skuFeedList",
        |        "version": 1,
        |        "originName": "rtb.advProductShelf",
        |        "originVertical": "rtb",
        |        "originComponent": "advProductShelf",
        |        "originVersion": 1,
        |        "id": 519255,
        |        "configId": 5710,
        |        "configDtId": 14297,
        |        "revisionId": 658263,
        |        "index": 1,
        |        "dtName": "sku.feedList",
        |        "timeSpent": 32411745
        |      },
        |      "widgetToken": "3de8aa7243d141adb4199cb7ff8a3e7d7903602c"
        |    },
        |    {
        |      "vertical": "catalog",
        |      "component": "soldOutHeader",
        |      "stateId": "soldOutHeader-189812-default-1",
        |      "params": {
        |        "separatorHeight": 2,
        |        "subtitle": "Подпишитесь и мы сообщим о поступлении",
        |        "title": "Товары не в наличии"
        |      },
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "catalog.soldOutHeader",
        |        "vertical": "catalog",
        |        "component": "soldOutHeader",
        |        "version": 1,
        |        "id": 189812,
        |        "revisionId": 113257,
        |        "index": 4,
        |        "timeSpent": 162848702
        |      },
        |      "widgetToken": "dcb0fa48d2b88b2d9ec5e720718cd79d801d3311"
        |    },
        |    {
        |      "vertical": "catalog",
        |      "component": "soldOutResultsV2",
        |      "stateId": "soldOutResultsV2-189813-default-1",
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "catalog.soldOutResultsV2",
        |        "vertical": "catalog",
        |        "component": "soldOutResultsV2",
        |        "version": 1,
        |        "id": 189813,
        |        "revisionId": 113258,
        |        "index": 5,
        |        "timeSpent": 162851313
        |      },
        |      "widgetToken": "d240d7b9eab7f7b99127876002f3152dae3c4344"
        |    }
        |  ],
        |  "layoutTrackingInfo": {
        |    "brandId": 27367890,
        |    "categoryId": 6563,
        |    "deviceType": "app",
        |    "layoutContainer": "default",
        |    "layoutId": 1571,
        |    "layoutPageIndex": 1,
        |    "layoutVersion": 65,
        |    "pageType": "category",
        |    "platform": "ios",
        |    "ruleId": 1389,
        |    "templateType": "mobile"
        |  },
        |  "pageInfo": {
        |    "url": "/category/6563?brand=27367890&layout_container=default&layout_page_index=1&page=0&sold_out_page=1",
        |    "pageType": "category",
        |    "context": "ozon",
        |    "ruleId": 1389,
        |    "layoutId": 1571,
        |    "layoutVersion": 65
        |  },
        |  "pageToken": "61e34eed90d2329a9df3dd4deae2e007d480b406",
        |  "products": {
        |    "skuFeedList": {
        |      "skuFeedList-519255-default-1": {}
        |    }
        |  },
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
        |      "totalFound": 1,
        |      "totalPages": 1
        |    },
        |    "context": "ozon",
        |    "layoutId": 1571,
        |    "layoutVersion": 65,
        |    "pageType": "category",
        |    "ruleId": 1389
        |  },
        |  "trackingPayloads": {}
        |}
      """.stripMargin

    val result = resultRawJson.jsonAs[Result].value

    val page             = Page(1, 1, 1)
    val category         = Category(6563L @@ Category.Id, "Концентраты и бальзамы для тела" @@ Category.Name, "Концентраты и бальзамы" @@ Catalog.Name)
    val soldOutResultsV2 = SoldOutResultsV2.Success(List.empty)
    val catalog          = Catalog(page, category, None, None, Some(soldOutResultsV2))

    result should be(Result(None, Some(catalog)))
  }
}
