package marketplace.models.yandex.market

import java.time.OffsetDateTime

import cats.Show
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

/** Контекст обработки запроса.
  *
  * @param id       Уникальный идентификатор запроса.
  * @param time     Дата и время выполнения запроса в формате ISO 8601.
  * @param region   Информация о регионе запроса.
  * @param currency Валюта запроса.
  * @param page     Информация о параметрах страницы запроса.
  */
@derive(loggable)
case class Context(
  id: Context.ContextId,
  time: OffsetDateTime,
  region: Region,
  currency: Currency,
  page: Page
)

object Context {
  implicit val circeDecoder: Decoder[Context] = deriveDecoder

  /** Уникальный идентификатор запроса.
    */
  object ContextId extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
  }
  type ContextId = ContextId.Type
}
