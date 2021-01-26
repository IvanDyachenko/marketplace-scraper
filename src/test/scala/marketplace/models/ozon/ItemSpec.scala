package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class ItemSpec extends AnyFlatSpec with Matchers {

  ignore should "decode Item.??? from a valid JSON" in {
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

    decode[Item](itemRawJson).isRight shouldBe true
    //decode[Item.???](itemRawJson).isRight shouldBe true
  }
}
