package marketplace.models.yandex.market

import cats.implicits._
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

/** Информация о параметрах страницы запроса.
  *
  * @param number     Номер страницы.
  * @param count      Размер страницы (количество элементов на странице).
  * @param total      Количество страниц в результате.
  * @param totalItems Общее число элементов.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
case class Page(number: Page.Number, count: Page.Count, total: Int, totalItems: Option[Int])

object Page {

  /** Номер страницы.
    */
  object Number extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("page")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Number = Number.Type

  /** Размер страницы (количество элементов на странице).
    */
  object Count extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("count")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Count = Count.Type

  implicit val circeDecoder: Decoder[Page] = deriveDecoder
  implicit val avroCodec: Codec[Page]      = Codec.derive[Page]
}
