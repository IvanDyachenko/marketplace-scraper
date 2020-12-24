package marketplace.models.yandex.market

import java.time.{OffsetDateTime, ZoneOffset}

import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

/** Контекст обработки запроса.
  *
  * @param id       Уникальный идентификатор запроса.
  * @param time     Дата и время выполнения запроса в формате ISO 8601.
  * @param region   Информация о регионе запроса.
  * @param currency Валюта запроса.
  * @param page     Информация о параметрах страницы запроса.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
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
  object ContextId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type ContextId = ContextId.Type

  implicit val circeDecoder: Decoder[Context]                   = deriveDecoder
  implicit val offsetDateTimeVulcanCodec: Codec[OffsetDateTime] =
    Codec.instant.imap(OffsetDateTime.ofInstant(_, ZoneOffset.UTC))(_.toInstant())
  implicit val avroCodec: Codec[Context]                        = Codec.derive[Context]
}
