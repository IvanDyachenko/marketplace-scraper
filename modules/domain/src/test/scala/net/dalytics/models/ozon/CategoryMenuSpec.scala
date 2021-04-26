package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

import supertagged.postfix._

class CategoryMenuSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode CategoryMenu from a valid JSON (circe)" in {
    val categoryMenuRawJson =
      """
        |{
        |  "catalog": {
        |    "categoryMenu": {
        |      "categoryMenu-281296-default-1": {
        |        "categories": [
        |          {
        |            "id": 14500,
        |            "name": "Дом и сад",
        |            "isAdult": false,
        |            "link": "/category/dom-i-sad-14500/",
        |            "deeplink": "ozon://category/dom-i-sad-14500/",
        |            "isActive": false,
        |            "cellTrackingInfo": {
        |              "type": "category",
        |              "id": "14500",
        |              "index": 1,
        |              "title": "Дом и сад"
        |            },
        |            "categories": [
        |              {
        |                "id": 15078,
        |                "name": "Текстиль",
        |                "isAdult": false,
        |                "link": "/category/tekstil-15078/",
        |                "deeplink": "ozon://category/tekstil-15078/",
        |                "isActive": false,
        |                "cellTrackingInfo": {
        |                  "type": "category",
        |                  "id": "15078",
        |                  "index": 1,
        |                  "title": "Текстиль"
        |                },
        |                "categories": [
        |                  {
        |                    "id": 15085,
        |                    "name": "Постельное белье",
        |                    "isAdult": false,
        |                    "link": "/category/postelnoe-bele-15085/",
        |                    "deeplink": "ozon://category/postelnoe-bele-15085/",
        |                    "isActive": false,
        |                    "cellTrackingInfo": {
        |                      "type": "category",
        |                      "id": "15085",
        |                      "index": 1,
        |                      "title": "Постельное белье"
        |                    },
        |                    "categories": [
        |                      {
        |                        "id": 15086,
        |                        "name": "Комплекты постельного белья",
        |                        "isAdult": false,
        |                        "link": "/category/komplekty-postelnogo-belya-15086/",
        |                        "deeplink": "ozon://category/komplekty-postelnogo-belya-15086/",
        |                        "isActive": true,
        |                        "cellTrackingInfo": {
        |                          "type": "category",
        |                          "id": "15086",
        |                          "index": 1,
        |                          "title": "Комплекты постельного белья"
        |                        },
        |                        "categories": [],
        |                        "redirectTo": ""
        |                      }
        |                    ],
        |                    "redirectTo": ""
        |                  }
        |                ],
        |                "redirectTo": ""
        |              }
        |            ],
        |            "redirectTo": ""
        |          }
        |        ],
        |        "modalUrl": "/modal/categoryMenu"
        |      }
        |    },
        |    "shared": {
        |      "catalog": {
        |        "activeSort": "",
        |        "brand": null,
        |        "breadCrumbs": null,
        |        "category": {
        |          "id": 15086,
        |          "name": "Комплекты постельного белья",
        |          "imageUrls": {
        |            "catalog_logo": "https://ozon-st.cdn.ngenix.net/multimedia/1025963144.jpg"
        |          },
        |          "isAdult": false,
        |          "catalogName": ""
        |        },
        |        "categoryPredicted": false,
        |        "context": "category",
        |        "correctedText": "",
        |        "currentPage": 0,
        |        "currentText": "",
        |        "currentUrl": "/modal/categoryMenu/category/komplekty-postelnogo-belya-15086/",
        |        "totalFound": 0,
        |        "totalPages": 0
        |      }
        |    }
        |  },
        |  "layout": [
        |    {
        |      "vertical": "catalog",
        |      "component": "categoryMenu",
        |      "stateId": "categoryMenu-281296-default-1",
        |      "version": 1,
        |      "widgetTrackingInfo": {
        |        "name": "catalog.categoryMenu",
        |        "vertical": "catalog",
        |        "component": "categoryMenu",
        |        "version": 1,
        |        "id": 281296,
        |        "revisionId": 472482,
        |        "index": 1,
        |        "timeSpent": 529248723
        |      },
        |      "widgetToken": ""
        |    }
        |  ],
        |  "layoutTrackingInfo": {
        |    "deviceType": "app",
        |    "layoutId": 8749,
        |    "layoutVersion": 13,
        |    "pageType": "modal/categoryMenu",
        |    "platform": "mobile_site",
        |    "ruleId": 8398,
        |    "templateType": "mobile"
        |  },
        |  "pageInfo": {
        |    "url": "/modal/categoryMenu/category/15086/",
        |    "pageType": "modal",
        |    "pageName": "categoryMenu",
        |    "context": "ozon",
        |    "ruleId": 8398,
        |    "layoutId": 8749,
        |    "layoutVersion": 13
        |  },
        |  "shared": {
        |    "catalog": {
        |      "activeSort": "",
        |      "brand": null,
        |      "breadCrumbs": null,
        |      "category": {
        |        "id": 15086,
        |        "name": "Комплекты постельного белья",
        |        "imageUrls": {
        |          "catalog_logo": "https://ozon-st.cdn.ngenix.net/multimedia/1025963144.jpg"
        |        },
        |        "isAdult": false,
        |        "catalogName": ""
        |      },
        |      "categoryPredicted": false,
        |      "context": "category",
        |      "correctedText": "",
        |      "currentPage": 0,
        |      "currentText": "",
        |      "currentUrl": "/modal/categoryMenu/category/komplekty-postelnogo-belya-15086/",
        |      "totalFound": 0,
        |      "totalPages": 0
        |    },
        |    "context": "ozon",
        |    "layoutId": 8749,
        |    "layoutVersion": 13,
        |    "pageName": "categoryMenu",
        |    "pageType": "modal",
        |    "ruleId": 8398
        |  }
        |}
      """.stripMargin

    val decodedResult = decode[Result](categoryMenuRawJson)

    decodedResult.value.categoryMenu.value shouldBe a[CategoryMenu]
  }

  it should "decode CategoryMenu from a valid JSON (tethys)" in {
    val categoryMenuRawJson =
      """
        |{
        |  "categoryMenu-281296-default-1": {
        |    "categories": [
        |      {
        |        "id": 14500,
        |        "name": "Дом и сад",
        |        "isAdult": false,
        |        "link": "/category/dom-i-sad-14500/",
        |        "deeplink": "ozon://category/dom-i-sad-14500/",
        |        "isActive": false,
        |        "cellTrackingInfo": {
        |          "type": "category",
        |          "id": "14500",
        |          "index": 1,
        |          "title": "Дом и сад"
        |        },
        |        "categories": [
        |          {
        |            "id": 15078,
        |            "name": "Текстиль",
        |            "isAdult": false,
        |            "link": "/category/tekstil-15078/",
        |            "deeplink": "ozon://category/tekstil-15078/",
        |            "isActive": false,
        |            "cellTrackingInfo": {
        |              "type": "category",
        |              "id": "15078",
        |              "index": 1,
        |              "title": "Текстиль"
        |            },
        |            "categories": [
        |              {
        |                "id": 15085,
        |                "name": "Постельное белье",
        |                "isAdult": false,
        |                "link": "/category/postelnoe-bele-15085/",
        |                "deeplink": "ozon://category/postelnoe-bele-15085/",
        |                "isActive": false,
        |                "cellTrackingInfo": {
        |                  "type": "category",
        |                  "id": "15085",
        |                  "index": 1,
        |                  "title": "Постельное белье"
        |                },
        |                "categories": [
        |                  {
        |                    "id": 15086,
        |                    "name": "Комплекты постельного белья",
        |                    "isAdult": false,
        |                    "link": "/category/komplekty-postelnogo-belya-15086/",
        |                    "deeplink": "ozon://category/komplekty-postelnogo-belya-15086/",
        |                    "isActive": true,
        |                    "cellTrackingInfo": {
        |                      "type": "category",
        |                      "id": "15086",
        |                      "index": 1,
        |                      "title": "Комплекты постельного белья"
        |                    },
        |                    "categories": [],
        |                    "redirectTo": ""
        |                  }
        |                ],
        |                "redirectTo": ""
        |              }
        |            ],
        |            "redirectTo": ""
        |          }
        |        ],
        |        "redirectTo": ""
        |      }
        |    ],
        |    "modalUrl": "/modal/categoryMenu"
        |  }
        |}
      """.stripMargin

    implicit val jsonReader = CategoryMenu.jsonReader(Component.CategoryMenu("categoryMenu-281296-default-1" @@ Component.StateId))
    val categoryMenu        = categoryMenuRawJson.jsonAs[CategoryMenu].value

    categoryMenu.categories.length should be(1)
  }
}
