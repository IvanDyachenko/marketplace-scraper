package net.dalytics.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class TemplateSpec extends AnyFlatSpec with Matchers {

  it should "decode Template.State.Action.Redirect from a valid JSON" in {
    val templateRawJson =
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

    decode[Template.State.Action](templateRawJson).isRight shouldBe true
    decode[Template.State.Action.Redirect.type](templateRawJson).isRight shouldBe true
  }

  it should "decode Template.State.TextSmall.NotDelivered from a valid JSON" in {
    val templateRawJson =
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

    decode[Template.State.TextSmall](templateRawJson).isRight shouldBe true
    decode[Template.State.TextSmall.NotDelivered.type](templateRawJson).isRight shouldBe true
  }

  it should "decode Template.State.TextSmall.PremiumPriority from a valid JSON" in {
    val templateRawJson =
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

    decode[Template.State.TextSmall](templateRawJson).isRight shouldBe true
    decode[Template.State.TextSmall.PremiumPriority.type](templateRawJson).isRight shouldBe true
  }
}
