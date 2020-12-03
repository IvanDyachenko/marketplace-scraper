package marketplace.models.yandex.market

import supertagged.TaggedType
import io.circe.syntax._
import io.circe.Encoder
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

import marketplace.models.yandex.market.requests.GetCategoryModels

import Request._

trait Request {
  def apiVersion: ApiVersion
  def path: String
  def uuid: User.UUID
  def geoId: Region.GeoId
  def page: Option[Page.Number]
  def count: Option[Page.Count]
  def fields: Fields
  def sections: Sections
  def rearrFactors: RearrFactors
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
