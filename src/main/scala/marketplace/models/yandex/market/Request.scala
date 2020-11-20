package marketplace.models.yandex.market

import supertagged.TaggedType
import io.circe.syntax._
import io.circe.Encoder
import org.http4s.{Headers, QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue, Uri}
import org.http4s.headers.{`User-Agent`, AgentComment, AgentProduct, Host}

import marketplace.models.{Request => BaseRequest}
import marketplace.models.yandex.market.headers._

import Request._
import marketplace.models.yandex.market.requests.GetCategoryModels

trait Request extends BaseRequest {
  val uri: Uri                         =
    Uri.unsafeFromString(s"https://mobile.market.yandex.net/market/blue/")
  val headers: Headers                 =
    Headers.of(
      Host("mobile.market.yandex.net"),
      `User-Agent`(AgentProduct("Beru", Some("323")), List(AgentComment("iPhone; iOS 14.0.1; Scale/3.00"))),
      `X-Device-Type`("SMARTPHONE"),
      `X-Platform`("IOS"),
      `X-App-Version`("3.2.3"),
      `X-Region-Id`(geoId)
    )
  def path: Uri.Path
  def uuid: User.UUID
  def geoId: Region.GeoId
  def fields: Fields
  def sections: Sections
  def rearrFactors: RearrFactors
  def queryParams: Map[String, String] =
    Map(
      QueryParam[User.UUID].key.value    -> QueryParamEncoder[User.UUID].encode(uuid).value,
      QueryParam[Region.GeoId].key.value -> QueryParamEncoder[Region.GeoId].encode(geoId).value,
      Fields.queryParam.key.value        -> Fields.queryParam.encode(fields).value,
      Sections.queryParam.key.value      -> Sections.queryParam.encode(sections).value,
      RearrFactors.queryParam.key.value  -> RearrFactors.queryParam.encode(rearrFactors).value
    )
}

object Request {

  /** Параметры категории, которые необходимо показать в выходных данных.
    */
  object Fields extends TaggedType[List[Field]] {
    implicit def fields(ls: List[Field]): Fields = Fields(ls)

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

  implicit val circeEncoder: Encoder[Request] = Encoder.instance { case categoryModels: GetCategoryModels => categoryModels.asJson }
}
