package net.dalytics.models.ozon

import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

class TemplateSpec extends AnyFlatSpec with Matchers with EitherValues with OptionValues {

  it should "decode Template.State.Action.Redirect from a valid JSON (circe)" in {
    val stateRawJson =
      """
        |{
        |  "type": "action",
        |  "id": "redirect",
        |  "components": null,
        |  "title": "Перейти",
        |  "activeTitle": "",
        |  "align": "bottomLeft",
        |  "isActive": false,
        |  "link": "/product/vnov-poverit-kitayu-29-aprelya-2020-shimov-yaroslav-gostev-aleksandr-216183532/",
        |  "deepLink": "ozon://web?url=https://ozon.ru/product/vnov-poverit-kitayu-29-aprelya-2020-shimov-yaroslav-gostev-aleksandr-216183532/",
        |  "isSubscribed": false
        |}
      """.stripMargin

    val state = decode[Template.State.Action](stateRawJson).value

    state should be(Template.State.Action.Redirect)
  }

  it should "decode Template.State.Action.Redirect from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "type": "action",
        |  "id": "redirect",
        |  "components": null,
        |  "title": "Перейти",
        |  "activeTitle": "",
        |  "align": "bottomLeft",
        |  "isActive": false,
        |  "link": "/product/vnov-poverit-kitayu-29-aprelya-2020-shimov-yaroslav-gostev-aleksandr-216183532/",
        |  "deepLink": "ozon://web?url=https://ozon.ru/product/vnov-poverit-kitayu-29-aprelya-2020-shimov-yaroslav-gostev-aleksandr-216183532/",
        |  "isSubscribed": false
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.Action].value

    state should be(Template.State.Action.Redirect)
  }

  it should "decode Template.State.Action.UniversalAction from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "type": "action",
        |  "id": "universalAction",
        |  "button": {
        |    "isActive": false,
        |    "default": {
        |      "type": "addToCartButtonWithQuantity",
        |      "addToCartButtonWithQuantity": {
        |        "text": "В корзину",
        |        "style": "STYLE_TYPE_PRIMARY",
        |        "maxItems": 2,
        |        "currentItems": 0,
        |        "action": {
        |          "id": "253507586",
        |          "quantity": 1
        |        }
        |      }
        |    }
        |  },
        |  "secondaryButton": {
        |    "isActive": false,
        |    "default": {
        |      "icon": "ic_m_kebab",
        |      "action": {
        |        "behavior": "BEHAVIOR_TYPE_ACTION_SHEET",
        |        "link": "catalogActionSheet?id=253507586"
        |      },
        |      "theme": "STYLE_TYPE_SECONDARY",
        |      "tintColor": "ozAccentPrimary"
        |    }
        |  }
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.Action].value

    state should be(Template.State.Action.UniversalAction(Button.AddToCartWithQuantity(1, 2)))
  }

  it should "decode Template.State.Action.AddToCartWithCount from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "type": "action",
        |  "id": "addToCartWithCount",
        |  "components": null,
        |  "title": "В корзину",
        |  "activeTitle": "",
        |  "align": "bottomLeft",
        |  "isActive": false,
        |  "maxItems": 51,
        |  "minItems": 1,
        |  "isSubscribed": false
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.Action].value

    state should be(Template.State.Action.AddToCartWithCount(1, 51))
  }

  it should "decode Template.State.TextSmall.NotDelivered from a valid JSON (circe)" in {
    val stateRawJson =
      """
        |{
        |  "type": "textSmall",
        |  "id": "notDelivered",
        |  "components": null,
        |  "items": null,
        |  "text": "Только для <b><font color='ozTextPrimary'>Premium</font></b>-подписчиков",
        |  "textColor": "ozGray60",
        |  "markupType": "html",
        |  "maxLines": 2
        |}
      """.stripMargin

    val state = decode[Template.State.TextSmall](stateRawJson).value

    state should be(Template.State.TextSmall.NotDelivered)
  }

  it should "decode Template.State.TextSmall.NotDelivered from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "type": "textSmall",
        |  "id": "notDelivered",
        |  "components": null,
        |  "items": null,
        |  "text": "Только для <b><font color='ozTextPrimary'>Premium</font></b>-подписчиков",
        |  "textColor": "ozGray60",
        |  "markupType": "html",
        |  "maxLines": 2
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.TextSmall].value

    state should be(Template.State.TextSmall.NotDelivered)
  }

  it should "decode Template.State.TextSmall.PremiumPriority from a valid JSON (circe)" in {
    val stateRawJson =
      """
        |{
        |  "type": "textSmall",
        |  "id": "premiumPriority",
        |  "components": null,
        |  "items": null,
        |  "text": "Только для <b><font color='ozTextPrimary'>Premium</font></b>-подписчиков",
        |  "textColor": "ozTextSecondary",
        |  "markupType": "html",
        |  "maxLines": 2
        |}
      """.stripMargin

    val state = decode[Template.State.TextSmall](stateRawJson).value

    state should be(Template.State.TextSmall.PremiumPriority)
  }

  it should "decode Template.State.TextSmall.PremiumPriority from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "type": "textSmall",
        |  "id": "premiumPriority",
        |  "components": null,
        |  "items": null,
        |  "text": "Только для <b><font color='ozTextPrimary'>Premium</font></b>-подписчиков",
        |  "textColor": "ozTextSecondary",
        |  "markupType": "html",
        |  "maxLines": 2
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.TextSmall].value

    state should be(Template.State.TextSmall.PremiumPriority)
  }

  it should "decode Template.State.Label.Type.New from a valid JSON (circe)" in {
    val stateRawJson =
      """
        |{
        |  "title": "Новинка",
        |  "isSelected": false,
        |  "color": null,
        |  "textColor": "ozAccentSecondary"
        |}
      """.stripMargin

    val state = decode[Template.State.Label.Type](stateRawJson).value

    state should be(Template.State.Label.Type.New)
  }

  it should "decode Template.State.Label.Type.New from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "title": "Новинка",
        |  "isSelected": false,
        |  "color": null,
        |  "textColor": "ozAccentSecondary"
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.Label.Type].value

    state should be(Template.State.Label.Type.New)
  }

  it should "decode Template.State.Label.Type.Bestseller from a valid JSON (circe)" in {
    val stateRawJson =
      """
        |{
        |  "title": "Бестселлер",
        |  "isSelected": false,
        |  "color": null,
        |  "textColor": "ozOrange"
        |}
      """.stripMargin

    val state = decode[Template.State.Label.Type](stateRawJson).value

    state should be(Template.State.Label.Type.Bestseller)
  }

  it should "decode Template.State.Label.Type.Bestseller from a valid JSON (tethys)" in {
    val stateRawJson =
      """
        |{
        |  "title": "Бестселлер",
        |  "isSelected": false,
        |  "color": null,
        |  "textColor": "ozOrange"
        |}
      """.stripMargin

    val state = stateRawJson.jsonAs[Template.State.Label.Type].value

    state should be(Template.State.Label.Type.Bestseller)
  }

  it should "decode Template from a valid JSON (tethys)" in {
    val templateRawJson =
      """
        |[
        |  {
        |    "type": "tileImage",
        |    "id": "tileImage",
        |    "images": [
        |      "https://cdn1.ozone.ru/multimedia/1025256625.jpg"
        |    ],
        |    "imageRatio": "1:1",
        |    "imageBadges": [
        |      {
        |        "coordinates": {
        |          "x": 1,
        |          "y": 5
        |        },
        |        "badge": {
        |          "text": "−37%",
        |          "tintColor": "ozWhite1",
        |          "backgroundColor": "ozAccentAlert",
        |          "theme": "STYLE_TYPE_DISCOUNT"
        |        }
        |      },
        |      {
        |        "coordinates": {
        |          "x": 3,
        |          "y": 5
        |        },
        |        "badge": {
        |          "text": "Express",
        |          "tintColor": "ozWhite1",
        |          "backgroundColor": "ozAccentSecondary",
        |          "theme": "STYLE_TYPE_MEDIUM"
        |        }
        |      }
        |    ]
        |  },
        |  {
        |    "type": "atom",
        |    "id": "atom",
        |    "atom": {
        |      "type": "price",
        |      "price": {
        |        "price": "869 ₽",
        |        "priceColor": "ozAccentAlert",
        |        "originalPrice": "1 399 ₽",
        |        "originalPriceColor": "ozTextPrimary",
        |        "theme": "STYLE_TYPE_MEDIUM"
        |      }
        |    }
        |  },
        |  {
        |    "type": "bigLabel",
        |    "id": "pricePerUnit",
        |    "components": null,
        |    "items": [
        |      {
        |        "title": "109 ₽ / шт",
        |        "isSelected": false,
        |        "color": null
        |      }
        |    ]
        |  },
        |  {
        |    "type": "title",
        |    "id": "name",
        |    "components": null,
        |    "items": null,
        |    "text": "Лакомство для собак мелких пород Мнямс \"Ассорти. Говядина, ягненок, курица\", 60 г х 8 шт",
        |    "textColor": "ozTextPrimary",
        |    "markupType": "",
        |    "maxLines": 0
        |  },
        |  {
        |    "type": "rating",
        |    "id": "rating",
        |    "components": null,
        |    "rating": 4.933330059051514,
        |    "commentsCount": 15,
        |    "title": "15 отзывов"
        |  },
        |  {
        |    "type": "action",
        |    "id": "favorite",
        |    "components": null,
        |    "title": "",
        |    "activeTitle": "",
        |    "align": "topRight",
        |    "isActive": false,
        |    "isSubscribed": false
        |  },
        |  {
        |    "type": "action",
        |    "id": "universalAction",
        |    "button": {
        |      "isActive": false,
        |      "default": {
        |        "type": "addToCartButtonWithQuantity",
        |        "addToCartButtonWithQuantity": {
        |          "text": "В корзину",
        |          "style": "STYLE_TYPE_PRIMARY",
        |          "maxItems": 1,
        |          "currentItems": 0,
        |          "action": {
        |            "id": "155945309",
        |            "quantity": 1
        |          }
        |        }
        |      }
        |    },
        |    "secondaryButton": {
        |      "isActive": false,
        |      "default": {
        |        "icon": "ic_m_kebab",
        |        "action": {
        |          "behavior": "BEHAVIOR_TYPE_ACTION_SHEET",
        |          "link": "catalogActionSheet?id=155945309"
        |        },
        |        "theme": "STYLE_TYPE_SECONDARY",
        |        "tintColor": "ozAccentPrimary"
        |      }
        |    }
        |  },
        |  {
        |    "type": "textSmall",
        |    "id": "deliveryInfo",
        |    "components": null,
        |    "items": null,
        |    "text": "<font color='ozTextPrimary'>OZON</font>, доставка и склад <font color='ozAccentPrimary'><b>OZON</b></font>",
        |    "textColor": "ozGray60",
        |    "markupType": "html",
        |    "maxLines": 3
        |  },
        |  {
        |    "type": "mobileContainer",
        |    "leftContainer": [
        |      {
        |        "type": "tileImage",
        |        "id": "tileImage",
        |        "images": [
        |          "https://cdn1.ozone.ru/s3/multimedia-5/6014619281.jpg"
        |        ],
        |        "imageRatio": "1:1.4"
        |      }
        |    ],
        |    "contentContainer": [
        |      {
        |        "type": "bigLabel",
        |        "id": "discount",
        |        "components": null,
        |        "items": [
        |          {
        |            "title": "−13%",
        |            "isSelected": false,
        |            "color": null,
        |            "textColor": "ozBGSecondary",
        |            "backgroundColor": "ozAccentAlert",
        |            "isBold": true
        |          }
        |        ]
        |      },
        |      {
        |        "type": "atom",
        |        "id": "atom",
        |        "atom": {
        |          "type": "price",
        |          "price": {
        |            "price": "1 483 ₽",
        |            "priceColor": "ozAccentAlert",
        |            "originalPrice": "1 706 ₽",
        |            "originalPriceColor": "ozTextPrimary",
        |            "theme": "STYLE_TYPE_MEDIUM"
        |          }
        |        }
        |      },
        |      {
        |        "type": "title",
        |        "id": "name",
        |        "components": null,
        |        "items": null,
        |        "text": "Твердые чернила Katun KT-886, черный, для лазерного принтера, совместимый",
        |        "textColor": "ozTextPrimary",
        |        "markupType": "",
        |        "maxLines": 0
        |      },
        |      {
        |        "type": "atom",
        |        "id": "atom",
        |        "atom": {
        |          "type": "multilineBadge",
        |          "multilineBadge": {
        |            "text": "<b><font color='ozTextPrimary'>Послезавтра</font></b>",
        |            "theme": "THEME_TYPE_THIN",
        |            "backgroundColor": "ozBGPrimary",
        |            "icon": "ic_s_car_outline",
        |            "iconTintColor": "ozTextQuaternary",
        |            "maxLines": 3,
        |            "testInfo": {
        |              "automatizationId": "deliveryPeriod"
        |            }
        |          }
        |        }
        |      },
        |      {
        |        "type": "textSmall",
        |        "id": "topAttributes",
        |        "components": null,
        |        "items": null,
        |        "text": "Тип: <font color='ozTextPrimary'>Твердые чернила</font>",
        |        "textColor": "ozTextSecondary",
        |        "markupType": "html",
        |        "maxLines": 100
        |      },
        |      {
        |        "type": "action",
        |        "id": "favorite",
        |        "components": null,
        |        "title": "",
        |        "activeTitle": "",
        |        "align": "topRight",
        |        "isActive": false,
        |        "isSubscribed": false
        |      }
        |    ],
        |    "footerContainer": [
        |      {
        |        "type": "action",
        |        "id": "universalAction",
        |        "button": {
        |          "isActive": false,
        |          "default": {
        |            "type": "addToCartButtonWithQuantity",
        |            "addToCartButtonWithQuantity": {
        |              "text": "В корзину",
        |              "style": "STYLE_TYPE_PRIMARY",
        |              "maxItems": 25,
        |              "currentItems": 0,
        |              "action": {
        |                "id": "182161075",
        |                "quantity": 1
        |              }
        |            }
        |          }
        |        },
        |        "secondaryButton": {
        |          "isActive": false,
        |          "default": {
        |            "icon": "ic_m_kebab",
        |            "action": {
        |              "behavior": "BEHAVIOR_TYPE_ACTION_SHEET",
        |              "link": "catalogActionSheet?id=182161075"
        |            },
        |            "theme": "STYLE_TYPE_SECONDARY",
        |            "tintColor": "ozAccentPrimary"
        |          }
        |        }
        |      },
        |      {
        |        "type": "textSmall",
        |        "id": "deliveryInfo",
        |        "components": null,
        |        "items": null,
        |        "text": "<font color='ozTextSecondary'>Продавец</font> <font color='ozTextPrimary'>Компания НИКОМ</font>",
        |        "textColor": "ozGray60",
        |        "markupType": "html",
        |        "maxLines": 3
        |      }
        |    ],
        |    "leftCols": 0,
        |    "rightCols": 0
        |  }
        |]
      """.stripMargin

    val template = templateRawJson.jsonAs[Template].value

    template should be(
      Template(
        List(
          Template.State.Unknown,
          Template.State.Unknown,
          Template.State.Unknown,
          Template.State.Unknown,
          Template.State.Unknown,
          Template.State.Action.Unknown,
          Template.State.Action.UniversalAction(Button.AddToCartWithQuantity(1, 1)),
          Template.State.TextSmall.Unknown,
          Template.State.MobileContainer(
            Template(
              List(
                Template.State.Action.UniversalAction(Button.AddToCartWithQuantity(1, 25)),
                Template.State.TextSmall.Unknown
              )
            )
          )
        )
      )
    )
  }
}
