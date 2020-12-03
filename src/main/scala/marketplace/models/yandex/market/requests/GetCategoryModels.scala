package marketplace.models.yandex.market.requests

import cats.implicits._
import io.circe.syntax._
import io.circe.{Encoder, Json}

import marketplace.models.yandex.market.{ApiVersion, Category, Field, Page, Region, Request, Section, User}
import marketplace.models.yandex.market.Request.{Fields, RearrFactors, Sections}
import marketplace.models.yandex.market.RearrFactor.{Name => RearrFactor}

final case class GetCategoryModels(
  uuid: User.UUID,
  geoId: Region.GeoId,
  categoryId: Category.CategoryId,
  pageNumber: Page.Number,
  pageCount: Page.Count
) extends Request {
  val apiVersion: ApiVersion = ApiVersion.`2.1.6`

  val path: String = s"market/blue/${apiVersion.show}/categories/${categoryId.show}/search"

  val page: Option[Page.Number] = Some(pageNumber)
  val count: Option[Page.Count] = Some(pageCount)

  val fields: Fields =
    List(
      Field.ModelVendor,
      Field.ModelCategory,
      Field.ModelPrice,
      Field.ModelDiscounts,
      Field.ModelRating,
      Field.ModelMedia,
      Field.ModelReasonsToBuy,
      Field.ModelSpecification,
      Field.ModelFilterColor,
      Field.ModelPhoto,
      Field.ModelPhotos,
      Field.ModelOffers,
      Field.ModelDefaultOffer,
      Field.OfferAll,
      Field.OfferShop,
      Field.OfferCategory,
      Field.OfferBundleSettings,
      Field.ShopAll
    )

  val sections: Sections = List(Section.Medicine)

  val rearrFactors: RearrFactors =
    List(
      RearrFactor.CommonlyPurchasedOrdered(),
      RearrFactor.MarketRebranded(1),
      RearrFactor.MarketRebranded(1),
      RearrFactor.MarketBlueSearchAuction(1),
      RearrFactor.MarketBlueSubjectFederationDistrict(1),
      RearrFactor.MarketBlueSearchAuctionSupplierBid(50),
      RearrFactor.MarketPromoBlueGenericBundle(1),
      RearrFactor.MarketPromoBlueCheapestAsGift4(1),
      RearrFactor.BuyerPriceNominalInclPromo(1)
    )
}

object GetCategoryModels {
  implicit val circeEncoder: Encoder[GetCategoryModels] = Encoder.instance(_ => Json.obj("cartSnapshot" -> List.empty[String].asJson))
}
