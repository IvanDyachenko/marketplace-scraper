package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class ResultSpec extends AnyFlatSpec with Matchers {

  it should "decode SearchResultsV2 from a valid JSON" in {
    val searchResultsV2RawJson =
      """
        |{
        |  "layout" : [
        |    {
        |      "stateId" : "searchResultsV2-189805-default-250",
        |      "vertical" : "catalog",
        |      "component" : "searchResultsV2",
        |      "version" : 1
        |    }
        |  ],
        |  "pageInfo" : {
        |    "layoutId" : 1571,
        |    "ruleId" : 1389,
        |    "pageType" : "category",
        |    "url" : "/category/korm-dlya-koshek-12348/?layout_container=default&layout_page_index=250&page=250",
        |    "context" : "ozon",
        |    "layoutVersion" : 65
        |  },
        |  "nextPage" : "/category/korm-dlya-koshek-12348/?layout_container=default&layout_page_index=251&page=251",
        |  "catalog" : {
        |    "searchResultsV2" : {
        |      "searchResultsV2-189805-default-250" : {
        |        "cols" : 12,
        |        "page" : 250,
        |        "items" : [
        |          {
        |            "isAdult" : false,
        |            "isAlcohol" : false,
        |            "cellTrackingInfo" : {
        |              "isSupermarket" : false,
        |              "deliveryTimeDiffDays" : -1,
        |              "id" : 215730070,
        |              "rating" : 0,
        |              "title" : "Корм сухой для котят с курицей, 400 г",
        |              "deliverySchema" : "FBS",
        |              "availability" : 1,
        |              "countItems" : 0,
        |              "price" : 519,
        |              "brandId" : 84897927,
        |              "isPersonalized" : false,
        |              "marketplaceSellerId" : 104111,
        |              "discount" : 23,
        |              "brand" : "MALUNGMA",
        |              "index" : 8965,
        |              "type" : "sku",
        |              "availableInDays" : 0,
        |              "marketingActionIds" : [
        |                11010979456230,
        |                11050497706160
        |              ],
        |              "category" : "Товары для животных/Для кошек/Корма и лакомства/Сухие корма/MALUNGMA",
        |              "isPromotedProduct" : false,
        |              "freeRest" : 5,
        |              "finalPrice" : 399
        |            },
        |            "template" : "search",
        |            "isInCompare" : false,
        |            "innerCols" : 0,
        |            "link" : "/product/korm-suhoy-dlya-kotyat-s-kuritsey-400-g-215730070/",
        |            "outerCols" : 0,
        |            "type" : "tile_builder",
        |            "isGrey" : false,
        |            "isInFavorites" : false,
        |            "deepLink" : "ozon://products/215730070/",
        |            "templateState" : [
        |              {
        |                "theme" : "default",
        |                "components" : null,
        |                "price" : "519 ₽",
        |                "type" : "price",
        |                "isPremium" : false,
        |                "id" : "price",
        |                "finalPrice" : "399 ₽"
        |              },
        |              {
        |                "type" : "label",
        |                "id" : "label",
        |                "items" : [
        |                  {
        |                    "title" : "Новинка",
        |                    "textColor" : "ozAccentSecondary",
        |                    "isSelected" : false,
        |                    "color" : null
        |                  }
        |                ],
        |                "components" : null
        |              },
        |              {
        |                "components" : null,
        |                "maxLines" : 2,
        |                "type" : "textSmall",
        |                "id" : "pricePerUnit",
        |                "theme" : "default",
        |                "markupType" : "",
        |                "text" : "100 ₽ / 100 гр",
        |                "textColor" : "ozTextPrimary",
        |                "items" : null
        |              },
        |              {
        |                "maxLines" : 0,
        |                "components" : null,
        |                "id" : "name",
        |                "type" : "title",
        |                "markupType" : "",
        |                "theme" : "default",
        |                "items" : null,
        |                "textColor" : "ozTextPrimary",
        |                "text" : "Корм сухой для котят с курицей, 400 г"
        |              },
        |              {
        |                "id" : "universalAction",
        |                "type" : "action",
        |                "button" : {
        |                  "isActive" : false,
        |                  "type" : "addToCartButtonWithQuantity",
        |                  "default" : {
        |                    "addToCartButtonWithQuantity" : {
        |                      "maxItems" : 700,
        |                      "currentItems" : 0,
        |                      "text" : "В корзину",
        |                      "style" : "STYLE_TYPE_PRIMARY",
        |                      "action" : {
        |                        "quantity" : 1,
        |                        "id" : "215730070"
        |                      }
        |                    }
        |                  }
        |                },
        |                "secondaryButton" : {
        |                  "isActive" : false,
        |                  "default" : {
        |                    "theme" : "STYLE_TYPE_SECONDARY",
        |                    "icon" : "ic_m_kebab",
        |                    "action" : {
        |                      "behavior" : "BEHAVIOR_TYPE_ACTION_SHEET",
        |                      "link" : "catalogActionSheet?id=215730070"
        |                    }
        |                  }
        |                }
        |              },
        |              {
        |                "theme" : "default",
        |                "markupType" : "html",
        |                "text" : "<font color='ozTextPrimary'>Moers</font>, доставка <font color='ozAccentPrimary'><b>OZON</b></font>, склад продавца",
        |                "textColor" : "ozGray60",
        |                "items" : null,
        |                "components" : null,
        |                "maxLines" : 3,
        |                "type" : "textSmall",
        |                "id" : "deliveryInfo"
        |              }
        |            ]
        |          },
        |          {
        |            "type": "tile_builder",
        |            "images": [
        |              "https://cdn1.ozone.ru/multimedia/1021895768.jpg"
        |            ],
        |            "isGrey": false,
        |            "isAdult": false,
        |            "isAlcohol": false,
        |            "link": "/product/uho-govyazhe-xxl-167053322/?asb=u6mY9z7ZQecw9ik4xEmJ5htiCN9V%252FaftFlg3%252FkmEVmM%253D",
        |            "deepLink": "ozon://products/167053322/?asb=u6mY9z7ZQecw9ik4xEmJ5htiCN9V%252FaftFlg3%252FkmEVmM%253D&miniapp=supermarket",
        |            "cellTrackingInfo": {
        |              "index": 145,
        |              "type": "sku",
        |              "id": 167053322,
        |              "title": "Ухо говяжье XXL",
        |              "availability": 1,
        |              "price": 223,
        |              "finalPrice": 203,
        |              "deliverySchema": "Retail",
        |              "marketplaceSellerId": 0,
        |              "category": "OZON Express/Товары для животных/Для собак/Корма и лакомства",
        |              "brand": "",
        |              "brandId": 0,
        |              "availableInDays": 0,
        |              "freeRest": 5,
        |              "stockCount": 5,
        |              "discount": 8,
        |              "marketingActionIds": [
        |                11006108938110,
        |                11050497706160
        |              ],
        |              "isPersonalized": false,
        |              "deliveryTimeDiffDays": -1,
        |              "isSupermarket": true,
        |              "isPromotedProduct": false,
        |              "rating": 4.885712623596191,
        |              "countItems": 105,
        |              "adv_second_bid": "u6mY9z7ZQecw9ik4xEmJ5htiCN9V/aftFlg3/kmEVmM=",
        |              "availableDeliverySchema": [
        |                131
        |              ]
        |            },
        |            "template": "search",
        |            "templateState": [
        |              {
        |                "type": "action",
        |                "id": "favorite",
        |                "components": null,
        |                "title": "",
        |                "activeTitle": "",
        |                "align": "topRight",
        |                "isActive": false,
        |                "isSubscribed": false
        |              },
        |              {
        |                "type": "price",
        |                "id": "price",
        |                "components": null,
        |                "price": "223 ₽",
        |                "finalPrice": "203 ₽",
        |                "isPremium": false
        |              },
        |              {
        |                "type" :"label",
        |                "id": "label",
        |                "components": null,
        |                "items": [
        |                  {
        |                    "title": "Бестселлер",
        |                    "isSelected": false,
        |                    "color": null,
        |                    "textColor": "ozOrange"
        |                  }
        |                ]
        |              },
        |              {
        |                "type": "title",
        |                "id": "name",
        |                "components": null,
        |                "items": null,
        |                "text": "Ухо говяжье XXL",
        |                "textColor": "ozTextPrimary",
        |                "markupType": "",
        |                "maxLines" :0
        |              },
        |              {
        |                "type": "rating",
        |                "id": "rating",
        |                "components": null,
        |                "rating": 4.885712623596191,
        |                "commentsCount": 105,
        |                "title": "105 отзывов"
        |              },
        |              {
        |                "type": "action",
        |                "id": "addToCartWithCount",
        |                "components": null,
        |                "title": "В корзину",
        |                "activeTitle": "",
        |                "align": "bottomLeft",
        |                "isActive": false,
        |                "maxItems": 51,
        |                "minItems": 1,
        |                "isSubscribed": false
        |              },
        |              {
        |                "type": "textSmall",
        |                "id": "deliveryInfo",
        |                "components": null,
        |                "items": null,
        |                "text": "<font color='ozTextPrimary'>OZON</font>, доставка со склада <font color='ozAccentPrimary'><b>OZON</b></font>",
        |                "textColor": "ozGray60",
        |                "markupType": "html",
        |                "maxLines": 3
        |              }
        |            ],
        |            "badges": [
        |              {
        |                "type": "text",
        |                "coordinates": { "x": 1, "y": 5 },
        |                "text": "−8%",
        |                "backgroundColor": "ozAccentAlert",
        |                "textColor": "ozWhite1",
        |                "isBold": true
        |              },
        |              {
        |                "type": "text",
        |                "coordinates": { "x": 3, "y": 5 },
        |                "text": "Express",
        |                "backgroundColor": "ozAccentSecondary",
        |                "textColor": "ozWhite1"
        |              }
        |            ],
        |            "isInFavorites": false,
        |            "isInCompare": false,
        |            "outerCols": 0,
        |            "innerCols": 0
        |          },
        |          {
        |            "isInFavorites" : false,
        |            "deepLink" : "ozon://products/202157525/",
        |            "outerCols" : 0,
        |            "type" : "tile_builder",
        |            "isGrey" : false,
        |            "innerCols" : 0,
        |            "link" : "/product/korm-suhoy-farmina-n-d-ocean-dlya-koshek-s-yagnenkom-i-chernikoy-5-kg-202157525/",
        |            "isInCompare" : false,
        |            "template" : "search",
        |            "isAlcohol" : false,
        |            "isAdult" : false,
        |            "cellTrackingInfo" : {
        |              "finalPrice" : 4152,
        |              "freeRest" : 0,
        |              "isPromotedProduct" : false,
        |              "marketingActionIds" : [
        |                11050497706160
        |              ],
        |              "category" : "Товары для животных/Для кошек/Корма и лакомства/Сухие корма/Farmina",
        |              "availableInDays" : 0,
        |              "type" : "sku",
        |              "index" : 8979,
        |              "brand" : "Farmina",
        |              "discount" : 0,
        |              "marketplaceSellerId" : 71713,
        |              "isPersonalized" : false,
        |              "brandId" : 138842619,
        |              "price" : 4152,
        |              "countItems" : 4,
        |              "availability" : 2,
        |              "title" : "Корм сухой Farmina N&D Ocean для кошек, с ягнёнком и черникой, 5 кг",
        |              "deliverySchema" : "FBS",
        |              "rating" : 5,
        |              "id" : 202157525,
        |              "deliveryTimeDiffDays" : -1,
        |              "isSupermarket" : false
        |            },
        |            "images" : [
        |              "https://cdn1.ozone.ru/s3/multimedia-y/6029288194.jpg"
        |            ],
        |            "badges" : [
        |              {
        |                "backgroundColor" : "ozTextSecondary",
        |                "text" : "Закончился",
        |                "textColor" : "ozWhite1",
        |                "coordinates" : {
        |                  "y" : 1,
        |                  "x" : 1
        |                },
        |                "type" : "text"
        |              }
        |            ],
        |            "templateState" : [
        |              {
        |                "isSubscribed" : false,
        |                "title" : "",
        |                "align" : "topRight",
        |                "components" : null,
        |                "activeTitle" : "",
        |                "isActive" : false,
        |                "id" : "favorite",
        |                "type" : "action"
        |              },
        |              {
        |                "type" : "price",
        |                "isPremium" : false,
        |                "id" : "price",
        |                "theme" : "default",
        |                "price" : "4 152 ₽",
        |                "components" : null
        |              },
        |              {
        |                "maxLines" : 0,
        |                "components" : null,
        |                "id" : "name",
        |                "type" : "title",
        |                "markupType" : "",
        |                "theme" : "default",
        |                "textColor" : "ozTextPrimary",
        |                "items" : null,
        |                "text" : "Корм сухой Farmina N&D Ocean для кошек, с ягнёнком и черникой, 5 кг"
        |              },
        |              {
        |                "type" : "rating",
        |                "id" : "rating",
        |                "rating" : 5,
        |                "commentsCount" : 4,
        |                "title" : "4 отзыва",
        |                "components" : null
        |              },
        |              {
        |                "id" : "universalAction",
        |                "secondaryButton" : {
        |                  "default" : {
        |                    "theme" : "STYLE_TYPE_SECONDARY",
        |                    "action" : {
        |                      "link" : "catalogActionSheet?id=202157525",
        |                      "behavior" : "BEHAVIOR_TYPE_ACTION_SHEET"
        |                    },
        |                    "icon" : "ic_m_kebab"
        |                  },
        |                  "isActive" : false
        |                },
        |                "type" : "action",
        |                "button" : {
        |                  "isActive" : false,
        |                  "default" : {
        |                    "type" : "smallButton",
        |                    "smallButton" : {
        |                      "action" : {
        |                        "behavior" : "BEHAVIOR_TYPE_COMPOSER_NESTED_PAGE",
        |                        "link" : "ozon://modal/analogs/?product_id=202157525"
        |                      },
        |                      "style" : "STYLE_TYPE_SECONDARY_SMALL",
        |                      "text" : "Похожие",
        |                      "theme" : "STYLE_TYPE_SECONDARY_SMALL"
        |                    }
        |                  }
        |                }
        |              }
        |            ]
        |          }
        |        ]
        |      }
        |    }
        |  }
        |}
      """.stripMargin

    decode[Result](searchResultsV2RawJson).isRight shouldBe true
    decode[Result.SearchResultsV2](searchResultsV2RawJson).isRight shouldBe true
  }

  it should "decode FailureSearchResultsV2 from a valid JSON" in {
    val failureSearchResultsV2RawJson =
      """
        |{
        |   "catalog" : {
        |      "shared" : {
        |         "catalog" : {
        |            "totalPages" : 70,
        |            "context" : "category",
        |            "currentUrl" : "/category/dlya-gryzunov-12429/?page=131",
        |            "categoryPredicted" : false,
        |            "correctedText" : "",
        |            "totalFound" : 2487,
        |            "currentText" : "",
        |            "category" : {
        |               "catalogName" : "",
        |               "imageUrls" : {
        |                  "catalog_logo" : "https://cdn1.ozone.ru/multimedia/1033937063.jpg"
        |               },
        |               "isAdult" : false,
        |               "name" : "Для грызунов",
        |               "id" : 12429
        |            },
        |            "currentPage" : 131,
        |            "brand" : null,
        |            "activeSort" : "",
        |            "breadCrumbs" : null
        |         }
        |      },
        |      "searchResultsV2" : {
        |         "searchResultsV2-189805-default-131" : {
        |            "error" : "internal server error"
        |         }
        |      }
        |   },
        |   "pageInfo" : {
        |      "context" : "ozon",
        |      "layoutId" : 1571,
        |      "url" : "/category/dlya-gryzunov-12429/?layout_container=default&layout_page_index=131&page=131",
        |      "layoutVersion" : 65,
        |      "pageType" : "category",
        |      "ruleId" : 1389
        |   },
        |   "nextPage" : "/category/dlya-gryzunov-12429/?layout_container=no_results&layout_page_index=132&page=131",
        |   "layout" : [
        |      {
        |         "vertical" : "catalog",
        |         "version" : 1,
        |         "stateId" : "searchResultsV2-189805-default-131",
        |         "component" : "searchResultsV2"
        |      }
        |   ]
        |}
      """.stripMargin

    decode[Result](failureSearchResultsV2RawJson).isRight shouldBe true
    decode[Result.FailureSearchResultsV2](failureSearchResultsV2RawJson).isRight shouldBe true
  }
}
