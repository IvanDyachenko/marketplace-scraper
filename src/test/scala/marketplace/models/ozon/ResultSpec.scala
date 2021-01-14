package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import io.circe.Json

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
