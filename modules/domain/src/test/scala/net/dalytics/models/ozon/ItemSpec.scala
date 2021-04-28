package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode
import tethys._
import tethys.jackson._

class ItemSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Item.InStock (AddToCart.With, 1) from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "isAdult" : false,
        |  "isAlcohol" : false,
        |  "cellTrackingInfo" : {
        |    "isSupermarket" : false,
        |    "deliveryTimeDiffDays" : -1,
        |    "id" : 215730070,
        |    "rating" : 0,
        |    "title" : "Корм сухой для котят с курицей, 400 г",
        |    "deliverySchema" : "FBS",
        |    "availability" : 1,
        |    "countItems" : 0,
        |    "price" : 519,
        |    "brandId" : 84897927,
        |    "isPersonalized" : false,
        |    "marketplaceSellerId" : 104111,
        |    "discount" : 23,
        |    "brand" : "MALUNGMA",
        |    "index" : 8965,
        |    "type" : "sku",
        |    "availableInDays" : 0,
        |    "marketingActionIds" : [
        |      11010979456230,
        |      11050497706160
        |    ],
        |    "category" : "Товары для животных/Для кошек/Корма и лакомства/Сухие корма/MALUNGMA",
        |    "isPromotedProduct" : false,
        |    "freeRest" : 5,
        |    "finalPrice" : 399
        |  },
        |  "template" : "search",
        |  "isInCompare" : false,
        |  "innerCols" : 0,
        |  "link" : "/product/korm-suhoy-dlya-kotyat-s-kuritsey-400-g-215730070/",
        |  "outerCols" : 0,
        |  "type" : "tile_builder",
        |  "isGrey" : false,
        |  "isInFavorites" : false,
        |  "deepLink" : "ozon://products/215730070/",
        |  "templateState" : [
        |    {
        |      "theme" : "default",
        |      "components" : null,
        |      "price" : "519 ₽",
        |      "type" : "price",
        |      "isPremium" : false,
        |      "id" : "price",
        |      "finalPrice" : "399 ₽"
        |    },
        |    {
        |      "type" : "label",
        |      "id" : "label",
        |      "items" : [
        |        {
        |          "title" : "Новинка",
        |          "textColor" : "ozAccentSecondary",
        |          "isSelected" : false,
        |          "color" : null
        |        }
        |      ],
        |      "components" : null
        |    },
        |    {
        |      "components" : null,
        |      "maxLines" : 2,
        |      "type" : "textSmall",
        |      "id" : "pricePerUnit",
        |      "theme" : "default",
        |      "markupType" : "",
        |      "text" : "100 ₽ / 100 гр",
        |      "textColor" : "ozTextPrimary",
        |      "items" : null
        |    },
        |    {
        |      "maxLines" : 0,
        |      "components" : null,
        |      "id" : "name",
        |      "type" : "title",
        |      "markupType" : "",
        |      "theme" : "default",
        |      "items" : null,
        |      "textColor" : "ozTextPrimary",
        |      "text" : "Корм сухой для котят с курицей, 400 г"
        |    },
        |    {
        |      "id" : "universalAction",
        |      "type" : "action",
        |      "button" : {
        |        "isActive" : false,
        |        "type" : "addToCartButtonWithQuantity",
        |        "default" : {
        |          "addToCartButtonWithQuantity" : {
        |            "maxItems" : 700,
        |            "currentItems" : 0,
        |            "text" : "В корзину",
        |            "style" : "STYLE_TYPE_PRIMARY",
        |            "action" : {
        |              "quantity" : 1,
        |              "id" : "215730070"
        |            }
        |          }
        |        }
        |      },
        |      "secondaryButton" : {
        |        "isActive" : false,
        |        "default" : {
        |          "theme" : "STYLE_TYPE_SECONDARY",
        |          "icon" : "ic_m_kebab",
        |          "action" : {
        |            "behavior" : "BEHAVIOR_TYPE_ACTION_SHEET",
        |            "link" : "catalogActionSheet?id=215730070"
        |          }
        |        }
        |      }
        |    },
        |    {
        |      "theme" : "default",
        |      "markupType" : "html",
        |      "text" : "<font color='ozTextPrimary'>Moers</font>, доставка <font color='ozAccentPrimary'><b>OZON</b></font>, склад продавца",
        |      "textColor" : "ozGray60",
        |      "items" : null,
        |      "components" : null,
        |      "maxLines" : 3,
        |      "type" : "textSmall",
        |      "id" : "deliveryInfo"
        |    }
        |  ]
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.With(1, 700))
    itemTethys.addToCart should be(AddToCart.With(1, 700))
  }

  it should "decode Item.InStock (AddToCart.With, 2) from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |    "https://cdn1.ozone.ru/multimedia/1021895768.jpg"
        |  ],
        |  "isGrey": false,
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/product/uho-govyazhe-xxl-167053322/?asb=u6mY9z7ZQecw9ik4xEmJ5htiCN9V%252FaftFlg3%252FkmEVmM%253D",
        |  "deepLink": "ozon://products/167053322/?asb=u6mY9z7ZQecw9ik4xEmJ5htiCN9V%252FaftFlg3%252FkmEVmM%253D&miniapp=supermarket",
        |  "cellTrackingInfo": {
        |    "index": 145,
        |    "type": "sku",
        |    "id": 167053322,
        |    "title": "Ухо говяжье XXL",
        |    "availability": 1,
        |    "price": 223,
        |    "finalPrice": 203,
        |    "deliverySchema": "Retail",
        |    "marketplaceSellerId": 0,
        |    "category": "OZON Express/Товары для животных/Для собак/Корма и лакомства",
        |    "brand": "",
        |    "brandId": 0,
        |    "availableInDays": 0,
        |    "freeRest": 5,
        |    "stockCount": 5,
        |    "discount": 8,
        |    "marketingActionIds": [
        |      11006108938110,
        |      11050497706160
        |    ],
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": -1,
        |    "isSupermarket": true,
        |    "isPromotedProduct": false,
        |    "rating": 4.885712623596191,
        |    "countItems": 105,
        |    "adv_second_bid": "u6mY9z7ZQecw9ik4xEmJ5htiCN9V/aftFlg3/kmEVmM=",
        |    "availableDeliverySchema": [
        |      131
        |    ]
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "action",
        |      "id": "favorite",
        |      "components": null,
        |      "title": "",
        |      "activeTitle": "",
        |      "align": "topRight",
        |      "isActive": false,
        |      "isSubscribed": false
        |    },
        |    {
        |      "type": "price",
        |      "id": "price",
        |      "components": null,
        |      "price": "223 ₽",
        |      "finalPrice": "203 ₽",
        |      "isPremium": false
        |    },
        |    {
        |      "type" :"label",
        |      "id": "label",
        |      "components": null,
        |      "items": [
        |        {
        |          "title": "Бестселлер",
        |          "isSelected": false,
        |          "color": null,
        |          "textColor": "ozOrange"
        |        }
        |      ]
        |    },
        |    {
        |      "type": "title",
        |      "id": "name",
        |      "components": null,
        |      "items": null,
        |      "text": "Ухо говяжье XXL",
        |      "textColor": "ozTextPrimary",
        |      "markupType": "",
        |      "maxLines" :0
        |    },
        |    {
        |      "type": "rating",
        |      "id": "rating",
        |      "components": null,
        |      "rating": 4.885712623596191,
        |      "commentsCount": 105,
        |      "title": "105 отзывов"
        |    },
        |    {
        |      "type": "action",
        |      "id": "addToCartWithCount",
        |      "components": null,
        |      "title": "В корзину",
        |      "activeTitle": "",
        |      "align": "bottomLeft",
        |      "isActive": false,
        |      "maxItems": 51,
        |      "minItems": 1,
        |      "isSubscribed": false
        |    },
        |    {
        |      "type": "textSmall",
        |      "id": "deliveryInfo",
        |      "components": null,
        |      "items": null,
        |      "text": "<font color='ozTextPrimary'>OZON</font>, доставка со склада <font color='ozAccentPrimary'><b>OZON</b></font>",
        |      "textColor": "ozGray60",
        |      "markupType": "html",
        |      "maxLines": 3
        |    }
        |  ],
        |  "badges": [
        |    {
        |      "type": "text",
        |      "coordinates": { "x": 1, "y": 5 },
        |      "text": "−8%",
        |      "backgroundColor": "ozAccentAlert",
        |      "textColor": "ozWhite1",
        |      "isBold": true
        |    },
        |    {
        |      "type": "text",
        |      "coordinates": { "x": 3, "y": 5 },
        |      "text": "Express",
        |      "backgroundColor": "ozAccentSecondary",
        |      "textColor": "ozWhite1"
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 0,
        |  "innerCols": 0
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.With(1, 51))
    itemTethys.addToCart should be(AddToCart.With(1, 51))
  }

  it should "decode Item.InStock (AddToCart.With, 3) from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |    "https://cdn1.ozone.ru/s3/multimedia-v/6024764539.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-n/6024764495.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-v/6024764503.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-3/6024764511.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-b/6024764519.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-r/6024764535.jpg"
        |  ],
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/product/chernila-dlya-zapravki-elecom-epson-epson-ic69rdh-sootvetstvuyushchie-chernye-chetyre-raza-the-200352826/",
        |  "deepLink": "ozon://products/200352826/",
        |  "cellTrackingInfo": {
        |    "index": 6,
        |    "type": "sku",
        |    "id": 200352826,
        |    "title": "Чернила Elecom B01N7VG32U",
        |    "availability": 1,
        |    "price": 2472,
        |    "finalPrice": 2332,
        |    "deliverySchema": "Crossborder",
        |    "marketplaceSellerId": 9170,
        |    "category": "Электроника/Офисная техника/Картриджи и расходные материалы/Твердые чернила/Elecom",
        |    "brand": "Elecom",
        |    "brandId": 26303114,
        |    "availableInDays": 0,
        |    "freeRest": 3,
        |    "stockCount": 3,
        |    "discount": 5,
        |    "marketingActionIds": [
        |      11010979456230,
        |      11050497706160
        |    ],
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": -1,
        |    "isSupermarket": false,
        |    "isPromotedProduct": false,
        |    "rating": 0,
        |    "countItems": 0,
        |    "isRfbs": true,
        |    "availableDeliverySchema": [
        |      232,
        |      223
        |    ]
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "mobileContainer",
        |      "leftContainer": [
        |        {
        |          "type": "tileImage",
        |          "id": "tileImage",
        |          "components": null,
        |          "images": [
        |            "https://cdn1.ozone.ru/s3/multimedia-v/6024764539.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-n/6024764495.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-v/6024764503.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-3/6024764511.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-b/6024764519.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-r/6024764535.jpg"
        |          ],
        |          "imageRatio": "1:1.4",
        |          "badges": [
        |            {
        |              "type": "text",
        |              "coordinates": {
        |                "x": 1,
        |                "y": 1
        |              },
        |              "text": "Ozon Global",
        |              "backgroundColor": "ozAccentAlert",
        |              "textColor": "ozWhite1"
        |            }
        |          ]
        |        }
        |      ],
        |      "contentContainer": [
        |        {
        |          "type": "bigLabel",
        |          "id": "discount",
        |          "components": null,
        |          "items": [
        |            {
        |              "title": "−5%",
        |              "isSelected": false,
        |              "color": null,
        |              "textColor": "ozBGSecondary",
        |              "backgroundColor": "ozAccentAlert",
        |              "isBold": true
        |            }
        |          ]
        |        },
        |        {
        |          "type": "price",
        |          "id": "price",
        |          "components": null,
        |          "price": "2 472 ₽",
        |          "finalPrice": "2 332 ₽",
        |          "isPremium": false
        |        },
        |        {
        |          "type": "title",
        |          "id": "name",
        |          "components": null,
        |          "items": null,
        |          "text": "Чернила Elecom B01N7VG32U",
        |          "textColor": "ozTextPrimary",
        |          "markupType": "",
        |          "maxLines": 0
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "topAttributes",
        |          "components": null,
        |          "items": null,
        |          "text": "Тип: <font color='ozTextPrimary'>Твердые чернила</font><br>Бренд: <font color='ozTextPrimary'>Elecom</font>",
        |          "textColor": "ozTextSecondary",
        |          "markupType": "html",
        |          "maxLines": 100
        |        },
        |        {
        |          "type": "action",
        |          "id": "favorite",
        |          "components": null,
        |          "title": "",
        |          "activeTitle": "",
        |          "align": "topRight",
        |          "isActive": false,
        |          "isSubscribed": false
        |        }
        |      ],
        |      "footerContainer": [
        |        {
        |          "type": "action",
        |          "id": "universalAction",
        |          "button": {
        |            "isActive": false,
        |            "default": {
        |              "type": "addToCartButtonWithQuantity",
        |              "addToCartButtonWithQuantity": {
        |                "text": "В корзину",
        |                "style": "STYLE_TYPE_PRIMARY",
        |                "maxItems": 3,
        |                "currentItems": 0,
        |                "action": {
        |                  "id": "200352826",
        |                  "quantity": 1
        |                }
        |              }
        |            }
        |          },
        |          "secondaryButton": {
        |            "isActive": false,
        |            "default": {
        |              "icon": "ic_m_kebab",
        |              "action": {
        |                "behavior": "BEHAVIOR_TYPE_ACTION_SHEET",
        |                "link": "catalogActionSheet?id=200352826"
        |              },
        |              "theme": "STYLE_TYPE_SECONDARY",
        |              "tintColor": "ozAccentPrimary"
        |            }
        |          }
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "deliveryInfo",
        |          "components": null,
        |          "items": null,
        |          "text": "<font color='ozTextPrimary'>want jp</font>, доставка из-за рубежа",
        |          "textColor": "ozGray60",
        |          "markupType": "html",
        |          "maxLines": 3
        |        }
        |      ],
        |      "leftCols": 0,
        |      "rightCols": 0
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 12,
        |  "innerCols": 12
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.With(1, 3))
    itemTethys.addToCart should be(AddToCart.With(1, 3))
  }

  it should "decode Item.InStock (AddToCart.Redirect, 1) from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |    "https://cdn1.ozone.ru/s3/multimedia-m/6032830294.jpg"
        |  ],
        |  "isGrey": false,
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/product/zhair-neveruyushchiy-20-maya-2020-shimov-yaroslav-gostev-aleksandr-216183527/?asb=ZhvrGys1qagQRoj6FdQKVTGCX4c9viUiG0A1UKjJjec%253D",
        |  "deepLink": "ozon://products/216183527/?asb=ZhvrGys1qagQRoj6FdQKVTGCX4c9viUiG0A1UKjJjec%253D",
        |  "cellTrackingInfo": {
        |    "index": 5257,
        |    "type": "sku",
        |    "id": 216183527,
        |    "title": "Жаир неверующий - 20 мая, 2020 | Шимов Ярослав, Гостев Александр",
        |    "availability": 1,
        |    "price": 1,
        |    "finalPrice": 1,
        |    "deliverySchema": "Retail",
        |    "marketplaceSellerId": 0,
        |    "category": "Книги/Аудиокниги",
        |    "brand": "",
        |    "brandId": 0,
        |    "availableInDays": 0,
        |    "freeRest": 1,
        |    "stockCount": 1,
        |    "discount": 0,
        |    "marketingActionIds": null,
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": 1,
        |    "isSupermarket": false,
        |    "isPromotedProduct": false,
        |    "rating": 0,
        |    "countItems": 0,
        |    "adv_second_bid": "ZhvrGys1qagQRoj6FdQKVTGCX4c9viUiG0A1UKjJjec=",
        |    "availableDeliverySchema": [
        |      111
        |    ],
        |    "credit_product_type": "credit,6",
        |    "credit_product_price": 1
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "action",
        |      "id": "favorite",
        |      "components": null,
        |      "title": "",
        |      "activeTitle": "",
        |      "align": "topRight",
        |      "isActive": false,
        |      "isSubscribed": false
        |    },
        |    {
        |      "type": "atom",
        |      "id": "atom",
        |      "atom": {
        |        "type": "price",
        |        "price": {
        |          "price": "1 ₽",
        |          "priceColor": "ozTextPrimary",
        |          "theme": "STYLE_TYPE_MEDIUM"
        |        }
        |      }
        |    },
        |    {
        |      "type": "label",
        |      "id": "label",
        |      "components": null,
        |      "items": [
        |        {
        |          "title": "Аудиокнига",
        |          "isSelected": false,
        |          "color": null,
        |          "textColor": "ozTextPrimary"
        |        }
        |      ]
        |    },
        |    {
        |      "type": "title",
        |      "id": "name",
        |      "components": null,
        |      "items": null,
        |      "text": "Жаир неверующий - 20 мая, 2020 | Шимов Ярослав, Гостев Александр",
        |      "textColor": "ozTextPrimary",
        |      "markupType": "",
        |      "maxLines": 0
        |    },
        |    {
        |      "type": "action",
        |      "id": "redirect",
        |      "components": null,
        |      "title": "Перейти",
        |      "activeTitle": "",
        |      "align": "bottomLeft",
        |      "isActive": false,
        |      "link": "/product/zhair-neveruyushchiy-20-maya-2020-shimov-yaroslav-gostev-aleksandr-216183527/",
        |      "deepLink": "ozon://web?url=https://ozon.ru/product/zhair-neveruyushchiy-20-maya-2020-shimov-yaroslav-gostev-aleksandr-216183527/",
        |      "isSubscribed": false
        |    },
        |    {
        |      "type": "textSmall",
        |      "id": "deliveryInfo",
        |      "components": null,
        |      "items": null,
        |      "text": "Доставка на электронную почту от <font color='ozAccentPrimary'><b>OZON</b></font>",
        |      "textColor": "ozGray60",
        |      "markupType": "html",
        |      "maxLines": 3
        |    }
        |  ],
        |  "badges": [
        |    {
        |      "type": "text",
        |      "coordinates": {
        |        "x": 3,
        |        "y": 5
        |      },
        |      "text": "Цифровой",
        |      "backgroundColor": "ozBluePale",
        |      "textColor": "ozBlack"
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 0,
        |  "innerCols": 0
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.Redirect)
    itemTethys.addToCart should be(AddToCart.Redirect)
  }

  it should "decode Item.InStock (AddToCart.PremiumOnly, 1) from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |    "https://cdn1.ozone.ru/s3/multimedia-z/6023338079.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-7/6023338087.jpg"
        |  ],
        |  "isGrey": false,
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/product/la-roche-posay-lipikar-balzam-ap-m-400-ml-174260511/",
        |  "deepLink": "ozon://products/174260511/",
        |  "cellTrackingInfo": {
        |    "index": 6,
        |    "type": "sku",
        |    "id": 174260511,
        |    "title": "La Roche-Posay Lipikar Бальзам АП+М, 400 мл",
        |    "availability": 1,
        |    "price": 1840,
        |    "finalPrice": 1472,
        |    "deliverySchema": "Retail",
        |    "marketplaceSellerId": 0,
        |    "category": "Красота и здоровье/Уход за телом/Увлажнение и питание/Концентраты и бальзамы/La Roche-Posay",
        |    "brand": "La Roche-Posay",
        |    "brandId": 32092844,
        |    "availableInDays": 0,
        |    "freeRest": 5,
        |    "stockCount": 5,
        |    "discount": 20,
        |    "marketingActionIds": [
        |      11286569889210,
        |      11050497706160
        |    ],
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": 3,
        |    "isSupermarket": false,
        |    "isPromotedProduct": false,
        |    "rating": 4.854369163513184,
        |    "countItems": 309,
        |    "customDimension4": "Доставит Ozon",
        |    "availableDeliverySchema": [
        |      111
        |    ],
        |    "credit_product_type": "credit,6",
        |    "credit_product_price": 263
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "action",
        |      "id": "favorite",
        |      "components": null,
        |      "title": "",
        |      "activeTitle": "",
        |      "align": "topRight",
        |      "isActive": false,
        |      "isSubscribed": false
        |    },
        |    {
        |      "type": "atom",
        |      "id": "atom",
        |      "atom": {
        |        "type": "price",
        |        "price": {
        |          "price": "1 472 ₽",
        |          "priceColor": "ozAccentAlert",
        |          "originalPrice": "1 840 ₽",
        |          "originalPriceColor": "ozTextPrimary",
        |          "theme": "STYLE_TYPE_MEDIUM"
        |        }
        |      }
        |    },
        |    {
        |      "type": "label",
        |      "id": "label",
        |      "components": null,
        |      "items": [
        |        {
        |          "title": "Бестселлер",
        |          "isSelected": false,
        |          "color": null,
        |          "textColor": "ozOrange"
        |        }
        |      ]
        |    },
        |    {
        |      "type": "title",
        |      "id": "name",
        |      "components": null,
        |      "items": null,
        |      "text": "La Roche-Posay Lipikar Бальзам АП+М, 400 мл",
        |      "textColor": "ozTextPrimary",
        |      "markupType": "",
        |      "maxLines": 0
        |    },
        |    {
        |      "type": "atom",
        |      "id": "atom",
        |      "atom": {
        |        "type": "multilineBadge",
        |        "multilineBadge": {
        |          "text": "Доставит Ozon",
        |          "backgroundColor": "ozBGPrimary",
        |          "maxLines": 3
        |        }
        |      }
        |    },
        |    {
        |      "type": "rating",
        |      "id": "rating",
        |      "components": null,
        |      "rating": 4.854369163513184,
        |      "commentsCount": 309,
        |      "title": "309 отзывов"
        |    },
        |    {
        |      "type": "variantsText",
        |      "id": "",
        |      "items": [
        |        {
        |          "title": "400",
        |          "isSelected": true,
        |          "color": []
        |        },
        |        {
        |          "title": "100",
        |          "isSelected": false,
        |          "color": []
        |        },
        |        {
        |          "title": "200",
        |          "isSelected": false,
        |          "color": []
        |        }
        |      ]
        |    },
        |    {
        |      "type": "textSmall",
        |      "id": "premiumPriority",
        |      "components": null,
        |      "items": null,
        |      "text": "Только для <b><font color='ozTextPrimary'>Premium</font></b>-подписчиков",
        |      "textColor": "ozTextSecondary",
        |      "markupType": "html",
        |      "maxLines": 2
        |    },
        |    {
        |      "type": "textSmall",
        |      "id": "deliveryInfo",
        |      "components": null,
        |      "items": null,
        |      "text": "<font color='ozTextSecondary'>Продавец</font> <font color='ozTextPrimary'>Ozon</font>",
        |      "textColor": "ozGray60",
        |      "markupType": "html",
        |      "maxLines": 3
        |    }
        |  ],
        |  "badges": [
        |    {
        |      "type": "text",
        |      "coordinates": {
        |        "x": 1,
        |        "y": 5
        |      },
        |      "text": "−20%",
        |      "backgroundColor": "ozAccentAlert",
        |      "textColor": "ozWhite1",
        |      "isBold": true
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 0,
        |  "innerCols": 0
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.PremiumOnly)
    itemTethys.addToCart should be(AddToCart.PremiumOnly)
  }

  it should "decode Item.InStock (AddToCart.PremiumOnly, 2) from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |    "https://cdn1.ozone.ru/multimedia/1024266018.jpg"
        |  ],
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/product/statsionarnyy-blender-kitfort-kt-1344-seryy-metallik-147090458/",
        |  "deepLink": "ozon://products/147090458/",
        |  "cellTrackingInfo": {
        |    "index": 6,
        |    "type": "sku",
        |    "id": 147090458,
        |    "title": "Стационарный блендер Kitfort КТ-1344, серый металлик",
        |    "availability": 1,
        |    "price": 4290,
        |    "finalPrice": 4290,
        |    "deliverySchema": "Retail",
        |    "marketplaceSellerId": 0,
        |    "category": "Бытовая техника/Техника для кухни/Миксеры, блендеры и измельчители/Блендеры/Стационарные/Kitfort",
        |    "brand": "Kitfort",
        |    "brandId": 28657369,
        |    "availableInDays": 0,
        |    "freeRest": 5,
        |    "stockCount": 5,
        |    "discount": 0,
        |    "marketingActionIds": [
        |      11050497706160
        |    ],
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": 1,
        |    "isSupermarket": false,
        |    "isPromotedProduct": false,
        |    "status": "цена с Premium;Есть дешевле внутри, от 3 690 ₽",
        |    "rating": 4.738100051879883,
        |    "countItems": 630,
        |    "availableDeliverySchema": [
        |      111
        |    ],
        |    "credit_product_type": "credit,6",
        |    "credit_product_price": 766
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "mobileContainer",
        |      "leftContainer": [
        |        {
        |          "type": "tileImage",
        |          "id": "tileImage",
        |          "components": null,
        |          "images": [
        |            "https://cdn1.ozone.ru/multimedia/1024266018.jpg"
        |          ],
        |          "imageRatio": "1:1.4",
        |          "badges": [
        |            {
        |              "type": "text",
        |              "coordinates": {
        |                "x": 1,
        |                "y": 1
        |              },
        |              "text": "-10% для Premium",
        |              "backgroundColor": "#7D36AA",
        |              "textColor": "#ffffff"
        |            }
        |          ]
        |        },
        |        {
        |          "type": "rating",
        |          "id": "rating",
        |          "components": null,
        |          "rating": 4.738100051879883,
        |          "commentsCount": 630,
        |          "title": "630 отзывов"
        |        }
        |      ],
        |      "contentContainer": [
        |        {
        |          "type": "atom",
        |          "id": "atom",
        |          "atom": {
        |            "type": "price",
        |            "price": {
        |              "price": "4 290 ₽",
        |              "priceColor": "ozTextPrimary",
        |              "theme": "STYLE_TYPE_MEDIUM"
        |            }
        |          }
        |        },
        |        {
        |          "type": "bigLabel",
        |          "id": "premiumPrice",
        |          "components": null,
        |          "items": [
        |            {
        |              "title": "3 411 ₽ с Premium",
        |              "isSelected": false,
        |              "color": null,
        |              "textColor": "ozTextPrimary"
        |            }
        |          ]
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "",
        |          "components": null,
        |          "items": null,
        |          "text": "Есть дешевле внутри, от 3 690 ₽",
        |          "textColor": "ozAccentPrimary",
        |          "markupType": "plain",
        |          "maxLines": 2
        |        },
        |        {
        |          "type": "title",
        |          "id": "name",
        |          "components": null,
        |          "items": null,
        |          "text": "Стационарный блендер Kitfort КТ-1344, серый металлик",
        |          "textColor": "ozTextPrimary",
        |          "markupType": "",
        |          "maxLines": 0
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "topAttributes",
        |          "components": null,
        |          "items": null,
        |          "text": "Мощность, Вт: <font color='ozTextPrimary'>800</font>",
        |          "textColor": "ozTextSecondary",
        |          "markupType": "html",
        |          "maxLines": 100
        |        },
        |        {
        |          "type": "action",
        |          "id": "favorite",
        |          "components": null,
        |          "title": "",
        |          "activeTitle": "",
        |          "align": "topRight",
        |          "isActive": false,
        |          "isSubscribed": false
        |        }
        |      ],
        |      "footerContainer": [
        |        {
        |          "type": "textSmall",
        |          "id": "notDelivered",
        |          "components": null,
        |          "items": null,
        |          "text": "Только для <b><font color='ozTextPrimary'>Premium</font></b>-подписчиков",
        |          "textColor": "ozGray60",
        |          "markupType": "html",
        |          "maxLines": 2
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "deliveryInfo",
        |          "components": null,
        |          "items": null,
        |          "text": "<font color='ozTextPrimary'>OZON</font>",
        |          "textColor": "ozGray60",
        |          "markupType": "html",
        |          "maxLines": 3
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "offers",
        |          "components": null,
        |          "items": null,
        |          "text": "2 предложения",
        |          "textColor": "ozTextPrimary",
        |          "markupType": "plain",
        |          "maxLines": 2
        |        }
        |      ],
        |      "leftCols": 0,
        |      "rightCols": 0
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 12,
        |  "innerCols": 12
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.PremiumOnly)
    itemTethys.addToCart should be(AddToCart.PremiumOnly)
  }

  it should "decode Item.OutOfStock from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "isInFavorites" : false,
        |  "deepLink" : "ozon://products/202157525/",
        |  "outerCols" : 0,
        |  "type" : "tile_builder",
        |  "isGrey" : false,
        |  "innerCols" : 0,
        |  "link" : "/product/korm-suhoy-farmina-n-d-ocean-dlya-koshek-s-yagnenkom-i-chernikoy-5-kg-202157525/",
        |  "isInCompare" : false,
        |  "template" : "search",
        |  "isAlcohol" : false,
        |  "isAdult" : false,
        |  "cellTrackingInfo" : {
        |    "finalPrice" : 4152,
        |    "freeRest" : 0,
        |    "isPromotedProduct" : false,
        |    "marketingActionIds" : [
        |      11050497706160
        |    ],
        |    "category" : "Товары для животных/Для кошек/Корма и лакомства/Сухие корма/Farmina",
        |    "availableInDays" : 0,
        |    "type" : "sku",
        |    "index" : 8979,
        |    "brand" : "Farmina",
        |    "discount" : 0,
        |    "marketplaceSellerId" : 71713,
        |    "isPersonalized" : false,
        |    "brandId" : 138842619,
        |    "price" : 4152,
        |    "countItems" : 4,
        |    "availability" : 2,
        |    "title" : "Корм сухой Farmina N&D Ocean для кошек, с ягнёнком и черникой, 5 кг",
        |    "deliverySchema" : "FBS",
        |    "rating" : 5,
        |    "id" : 202157525,
        |    "deliveryTimeDiffDays" : -1,
        |    "isSupermarket" : false
        |  },
        |  "images" : [
        |    "https://cdn1.ozone.ru/s3/multimedia-y/6029288194.jpg"
        |  ],
        |  "badges" : [
        |    {
        |      "backgroundColor" : "ozTextSecondary",
        |      "text" : "Закончился",
        |      "textColor" : "ozWhite1",
        |      "coordinates" : {
        |        "y" : 1,
        |        "x" : 1
        |      },
        |      "type" : "text"
        |    }
        |  ],
        |  "templateState" : [
        |    {
        |      "isSubscribed" : false,
        |      "title" : "",
        |      "align" : "topRight",
        |      "components" : null,
        |      "activeTitle" : "",
        |      "isActive" : false,
        |      "id" : "favorite",
        |      "type" : "action"
        |    },
        |    {
        |      "type" : "price",
        |      "isPremium" : false,
        |      "id" : "price",
        |      "theme" : "default",
        |      "price" : "4 152 ₽",
        |      "components" : null
        |    },
        |    {
        |      "maxLines" : 0,
        |      "components" : null,
        |      "id" : "name",
        |      "type" : "title",
        |      "markupType" : "",
        |      "theme" : "default",
        |      "textColor" : "ozTextPrimary",
        |      "items" : null,
        |      "text" : "Корм сухой Farmina N&D Ocean для кошек, с ягнёнком и черникой, 5 кг"
        |    },
        |    {
        |      "type" : "rating",
        |      "id" : "rating",
        |      "rating" : 5,
        |      "commentsCount" : 4,
        |      "title" : "4 отзыва",
        |      "components" : null
        |    },
        |    {
        |      "id" : "universalAction",
        |      "secondaryButton" : {
        |        "default" : {
        |          "theme" : "STYLE_TYPE_SECONDARY",
        |          "action" : {
        |            "link" : "catalogActionSheet?id=202157525",
        |            "behavior" : "BEHAVIOR_TYPE_ACTION_SHEET"
        |          },
        |          "icon" : "ic_m_kebab"
        |        },
        |        "isActive" : false
        |      },
        |      "type" : "action",
        |      "button" : {
        |        "isActive" : false,
        |        "default" : {
        |          "type" : "smallButton",
        |          "smallButton" : {
        |            "action" : {
        |              "behavior" : "BEHAVIOR_TYPE_COMPOSER_NESTED_PAGE",
        |              "link" : "ozon://modal/analogs/?product_id=202157525"
        |            },
        |            "style" : "STYLE_TYPE_SECONDARY_SMALL",
        |            "text" : "Похожие",
        |            "theme" : "STYLE_TYPE_SECONDARY_SMALL"
        |          }
        |        }
        |      }
        |    }
        |  ]
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.With(0, 0))
    itemTethys.addToCart should be(AddToCart.With(0, 0))
  }

  it should "decode Item.CannotBeShipped from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |    "https://cdn1.ozone.ru/s3/multimedia-r/6004331943.jpg"
        |  ],
        |  "isGrey": false,
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/product/suhoy-korm-dlya-sobak-royal-canin-mini-adult-4-kg-161351424/",
        |  "deepLink": "ozon://products/161351424/",
        |  "cellTrackingInfo": {
        |    "index": 8773,
        |    "type": "sku",
        |    "id": 161351424,
        |    "title": "Сухой корм для собак Royal Canin Mini Adult 4 кг",
        |    "availability": 6,
        |    "price": 1836,
        |    "finalPrice": 1836,
        |    "deliverySchema": "FBS",
        |    "marketplaceSellerId": 14526,
        |    "category": "Товары для животных/Для собак/Корма и лакомства/Сухие корма/Royal Canin",
        |    "brand": "Royal Canin","brandId":19060364,
        |    "availableInDays": 0,
        |    "freeRest": 0,
        |    "stockCount": 0,
        |    "discount": 0,
        |    "marketingActionIds": [
        |      11050497706160
        |    ],
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": -1,
        |    "isSupermarket": false,
        |    "isPromotedProduct": false,
        |    "rating": 0,
        |    "countItems": 0
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "action",
        |      "id": "favorite",
        |      "components": null,
        |      "title": "",
        |      "activeTitle": "",
        |      "align": "topRight",
        |      "isActive": false,
        |      "isSubscribed": false
        |    },
        |    {
        |      "type": "price",
        |      "id": "price",
        |      "components": null,
        |      "price": "1 836 ₽",
        |      "isPremium": false
        |    },
        |    {
        |      "type": "title",
        |      "id": "name",
        |      "components": null,
        |      "items": null,
        |      "text": "Сухой корм для собак Royal Canin Mini Adult 4 кг",
        |      "textColor": "ozTextPrimary",
        |      "markupType": "",
        |      "maxLines": 0
        |    },
        |    {
        |      "type": "textSmall",
        |      "id": "",
        |      "components": null,
        |      "items": null,
        |      "text": "Не доставляется в ваш регион",
        |      "markupType": "",
        |      "maxLines": 2
        |    },
        |    {
        |      "type": "textSmall",
        |      "id": "deliveryInfo",
        |      "components": null,
        |      "items": null,
        |      "text": "<font color='ozTextPrimary'>ООО Дискавери</font>, доставка <font color='ozAccentPrimary'><b>OZON</b></font>, склад продавца",
        |      "textColor": "ozGray60",
        |      "markupType":"html",
        |      "maxLines":3
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 0,
        |  "innerCols": 0
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.Unavailable)
    itemTethys.addToCart should be(AddToCart.Unavailable)
  }

  it should "decode Item.Preorder from a valid JSON" in {
    val itemRawJson =
      """
        |{
        |  "type": "tile_builder",
        |  "images": [
        |  "https://cdn1.ozone.ru/s3/multimedia-p/6032293321.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-l/6032293425.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-e/6032293490.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-0/6032293512.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-k/6032293532.jpg",
        |    "https://cdn1.ozone.ru/s3/multimedia-q/6032293682.jpg"
        |  ],
        |  "isAdult": false,
        |  "isAlcohol": false,
        |  "link": "/context/detail/id/208055748/",
        |  "deepLink": "ozon://products/208055748/",
        |  "cellTrackingInfo": {
        |    "index": 17,
        |    "type": "sku",
        |    "id": 208055748,
        |    "title": "Умная колонка Яндекс.Станция Макс, черный",
        |    "availability": 3,
        |    "price": 18990,
        |    "finalPrice": 18990,
        |    "deliverySchema": "Retail",
        |    "marketplaceSellerId": 0,
        |    "category": "Электроника/Наушники и аудиотехника/Акустика и колонки/Умные колонки/Яндекс",
        |    "brand": "Яндекс",
        |    "brandId": 13013270,
        |    "availableInDays": 0,
        |    "freeRest": 0,
        |    "stockCount": 0,
        |    "discount": 0,
        |    "marketingActionIds": [
        |      11050497706160
        |    ],
        |    "isPersonalized": false,
        |    "deliveryTimeDiffDays": -1,
        |    "isSupermarket": false,
        |    "isPromotedProduct": false,
        |    "rating": 4.711110591888428,
        |    "countItems": 90
        |  },
        |  "template": "search",
        |  "templateState": [
        |    {
        |      "type": "mobileContainer",
        |      "leftContainer": [
        |        {
        |          "type": "tileImage",
        |          "id": "tileImage",
        |          "components": null,
        |          "images": [
        |            "https://cdn1.ozone.ru/s3/multimedia-p/6032293321.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-l/6032293425.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-e/6032293490.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-0/6032293512.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-k/6032293532.jpg",
        |            "https://cdn1.ozone.ru/s3/multimedia-q/6032293682.jpg"
        |          ],
        |          "imageRatio": "1:1.4",
        |          "badges": [
        |            {
        |              "type": "text",
        |              "coordinates": {
        |                "x": 3,
        |                "y": 5
        |              },
        |              "text": "Предзаказ",
        |              "backgroundColor": "#004ed6",
        |              "textColor": "ozWhite1"
        |            }
        |          ]
        |        },
        |        {
        |          "type": "rating",
        |          "id": "rating",
        |          "components": null,
        |          "rating": 4.711110591888428,
        |          "commentsCount": 90,
        |          "title": "90 отзывов"
        |        }
        |      ],
        |      "contentContainer": [
        |        {
        |          "type": "price",
        |          "id": "price",
        |          "components": null,
        |          "price": "18 990 ₽",
        |          "isPremium": false
        |        },
        |        {
        |          "type": "label",
        |          "id": "label",
        |          "components": null,
        |          "items": [
        |            {
        |              "title": "В продаже с 27.02.2021",
        |              "isSelected": false,
        |              "color": null,
        |              "textColor": "ozAccentPrimary"
        |            }
        |          ]
        |        },
        |        {
        |          "type": "title",
        |          "id": "name",
        |          "components": null,
        |          "items": null,
        |          "text": "Умная колонка Яндекс.Станция Макс, черный",
        |          "textColor": "ozTextPrimary",
        |          "markupType": "",
        |          "maxLines": 0
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "topAttributes",
        |          "components": null,
        |          "items": null,
        |          "text": "Максимальная мощность, Вт",
        |          "textColor": "ozTextSecondary",
        |          "markupType": "html",
        |          "maxLines": 100
        |        },
        |        {
        |          "type": "action",
        |          "id": "favorite",
        |          "components": null,
        |          "title": "",
        |          "activeTitle": "",
        |          "align": "topRight",
        |          "isActive": false,
        |          "isSubscribed": false
        |        }
        |      ],
        |      "footerContainer": [
        |        {
        |          "type": "action",
        |          "id": "redirect",
        |          "components": null,
        |          "title": "Перейти",
        |          "activeTitle": "",
        |          "align": "bottomLeft",
        |          "isActive": false,
        |          "link": "/context/detail/id/208055748/",
        |          "deepLink": "ozon://web?url=https://ozon.ru/context/detail/id/208055748/",
        |          "isSubscribed": false
        |        },
        |        {
        |          "type": "textSmall",
        |          "id": "deliveryInfo",
        |          "components": null,
        |          "items": null,
        |          "text": "<font color='ozTextPrimary'>OZON</font>, доставка и склад <font color='ozAccentPrimary'><b>OZON</b></font>",
        |          "textColor": "ozGray60",
        |          "markupType": "html",
        |          "maxLines": 3
        |        }
        |      ],
        |      "leftCols": 0,
        |      "rightCols": 0
        |    }
        |  ],
        |  "isInFavorites": false,
        |  "isInCompare": false,
        |  "outerCols": 12,
        |  "innerCols": 12
        |}
      """.stripMargin

    val itemCirce  = decode[Item](itemRawJson).value
    val itemTethys = itemRawJson.jsonAs[Item].value

    itemCirce.addToCart should be(AddToCart.Unavailable)
    itemTethys.addToCart should be(AddToCart.Unavailable)
  }
}
