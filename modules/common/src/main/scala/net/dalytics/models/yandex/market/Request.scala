package net.dalytics.models.yandex.market

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}
import tofu.logging.Loggable
import supertagged.TaggedType

import net.dalytics.models.yandex.market.Request.{Fields, RearrFactors, Sections}

@derive(loggable)
sealed trait Request {
  def host: String = "mobile.market.yandex.net"
  def apiVersion: ApiVersion
  def method: String
  def uuid: User.UUID
  def geoId: Region.GeoId
  def page: Option[Page.Number]
  def count: Option[Page.Count]
  def fields: Request.Fields
  def sections: Request.Sections
  def rearrFactors: Request.RearrFactors
}

@derive(loggable)
final case class GetCategoryModels(
  uuid: User.UUID,
  geoId: Region.GeoId,
  categoryId: Category.CategoryId,
  pageNumber: Page.Number,
  pageCount: Page.Count,
  @masked(MaskMode.Erase) fields: Request.Fields,
  @masked(MaskMode.Erase) sections: Request.Sections,
  @masked(MaskMode.Erase) rearrFactors: Request.RearrFactors
) extends Request {
  val apiVersion: ApiVersion    = ApiVersion.`2.1.6`
  val method: String            = s"market/blue/${apiVersion.show}/categories/${categoryId.show}/search"
  val page: Option[Page.Number] = Some(pageNumber)
  val count: Option[Page.Count] = Some(pageCount)
}

object Request {

  /** Параметры категории, которые необходимо показать в выходных данных.
    */
  object Fields extends TaggedType[List[Field]] {
    implicit def fields(ls: List[Field]): Fields = Fields(ls)
    implicit val loggable: Loggable[Type]        = lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                       = QueryParameterKey("fields")
      def encode(values: Type): QueryParameterValue = QueryParameterValue(values.map(_.entryName).mkString(","))
    }
  }
  type Fields = Fields.Type

  /**
    */
  object Sections extends TaggedType[List[Section]] {
    implicit def sections(ls: List[Section]): Sections = Sections(ls)
    implicit val loggable: Loggable[Type]              = lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                       = QueryParameterKey("sections")
      def encode(values: Type): QueryParameterValue = QueryParameterValue(values.map(_.entryName).mkString(","))
    }
  }
  type Sections = Sections.Type

  /**
    */
  object RearrFactors extends TaggedType[List[RearrFactor]] {
    implicit def rearrFactors(ls: List[RearrFactor]): RearrFactors = RearrFactors(ls)
    implicit val loggable: Loggable[Type]                          = lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key = QueryParameterKey("rearr_factors")

      def encode(values: Type): QueryParameterValue =
        QueryParameterValue(
          values
            .map { case RearrFactor(name, value) => value.map(v => s"${name.entryName}=$v").getOrElse(name.entryName) }
            .mkString(";")
        )
    }
  }
  type RearrFactors = RearrFactors.Type

  implicit val circeEncoder: Encoder[Request] = Encoder.instance { case request: GetCategoryModels => request.asJson }
}

object GetCategoryModels {

  def apply(
    uuid: User.UUID,
    geoId: Region.GeoId,
    categoryId: Category.CategoryId,
    pageNumber: Page.Number,
    pageCount: Page.Count
  ): GetCategoryModels = {

    val fields: Fields = List(
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
        RearrFactor.Name.CommonlyPurchasedOrdered(),
        RearrFactor.Name.MarketRebranded(1),
        RearrFactor.Name.MarketRebranded(1),
        RearrFactor.Name.MarketBlueSearchAuction(1),
        RearrFactor.Name.MarketBlueSubjectFederationDistrict(1),
        RearrFactor.Name.MarketBlueSearchAuctionSupplierBid(50),
        RearrFactor.Name.MarketPromoBlueGenericBundle(1),
        RearrFactor.Name.MarketPromoBlueCheapestAsGift4(1),
        RearrFactor.Name.BuyerPriceNominalInclPromo(1)
      )

    GetCategoryModels(uuid, geoId, categoryId, pageNumber, pageCount, fields, sections, rearrFactors)
  }

  implicit val circeEncoder: Encoder[GetCategoryModels] = Encoder.instance(_ => Json.obj("cartSnapshot" -> List.empty[String].asJson))

//implicit val avroCodec: Codec[GetCategoryModels] =
//  Codec.record[GetCategoryModels](
//    name = "GetCategoryModels",
//    namespace = "yandex.market.models"
//  ) { field =>
//    field("host", _.host) *> field("apiVersion", _.apiVersion) *> field("method", _.method) *>
//      (
//        field("uuid", _.uuid),
//        field("geoId", _.geoId),
//        field("categoryId", _.categoryId),
//        field("page", _.pageNumber),
//        field("count", _.pageCount),
//        field("fields", _.fields),
//        field("sections", _.sections),
//        field("rearrFactors", _.rearrFactors)
//      ).mapN(GetCategoryModels.apply)
//  }
}
