package marketplace.models.yandex.market

import java.time.OffsetDateTime

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Контекст обработки запроса.
  *
  * @param id       Уникальный идентификатор запроса.
  * @param time     Дата и время выполнения запроса в формате ISO 8601.
  * @param region   Информация о регионе запроса.
  * @param currency Валюта запроса.
  * @param page     Информация о параметрах страницы запроса.
  */
@derive(loggable, decoder)
case class Context(
  id: Context.ContextId,
  time: OffsetDateTime,
  region: Region,
  currency: Currency,
  page: Page
)

object Context {

  /** Уникальный идентификатор запроса.
    */
  object ContextId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type ContextId = ContextId.Type

//implicit val offsetDateTimeVulcanCodec: Codec[OffsetDateTime] =
//  Codec.instant.imap(OffsetDateTime.ofInstant(_, ZoneOffset.UTC))(_.toInstant())
}
