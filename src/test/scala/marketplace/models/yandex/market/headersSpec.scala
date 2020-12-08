package marketplace.models.yandex.market

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import marketplace.models.yandex.market.headers._
import marketplace.models.yandex.market.Region

class headersSpec extends AnyFlatSpec with Matchers {

  it should "decode `X-Region-Id` header from a valid raw string" in {
    `X-Region-Id`.parse("213").isRight shouldBe true
    `X-Region-Id`.parse("213").map(Some(_)) shouldBe Right(Region.GeoId(213).map(`X-Region-Id`(_)))
  }

  it should "not decode `X-Region-Id` header from a valid raw string" in {
    `X-Region-Id`.parse("wrong").isLeft shouldBe true
  }
}
