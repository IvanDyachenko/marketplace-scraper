package marketplace.models.yandex.market

import cats.Show
import cats.implicits._
import supertagged.TaggedType
import vulcan.Codec
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

/** Информация о параметрах страницы запроса.
  *
  * @param number     Номер страницы.
  * @param count      Размер страницы (количество элементов на странице).
  * @param total      Количество страниц в результате.
  * @param totalItems Общее число элементов.
  */
@derive(loggable)
case class Page(number: Page.Number, count: Page.Count, total: Int, totalItems: Option[Int])

object Page {
  implicit val circeDecoder: Decoder[Page] = deriveDecoder

  /** Номер страницы.
    */
  object Number extends TaggedType[Int] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = Loggable.intLoggable.contramap(identity)
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val vulcanCodec: Codec[Type]    = lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("page")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Number = Number.Type

  /** Размер страницы (количество элементов на странице).
    */
  object Count extends TaggedType[Int] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = Loggable.intLoggable.contramap(identity)
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val vulcanCodec: Codec[Type]    = lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("count")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Count = Count.Type
}
