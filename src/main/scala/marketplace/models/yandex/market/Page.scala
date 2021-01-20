package marketplace.models.yandex.market

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Информация о параметрах страницы запроса.
  *
  * @param number     Номер страницы.
  * @param count      Размер страницы (количество элементов на странице).
  * @param total      Количество страниц в результате.
  * @param totalItems Общее число элементов.
  */
@derive(loggable, decoder)
case class Page(number: Page.Number, count: Page.Count, total: Int, totalItems: Option[Int])

object Page {

  /** Номер страницы.
    */
  object Number extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("page")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Number = Number.Type

  /** Размер страницы (количество элементов на странице).
    */
  object Count extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("count")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Count = Count.Type
}
