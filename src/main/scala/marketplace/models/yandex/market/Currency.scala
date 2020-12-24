package marketplace.models.yandex.market

import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

/** Валюта запроса.
  *
  * @param id   Код валюты.
  * @param name Название валюты.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
case class Currency(id: Currency.CurrencyId, name: String)

object Currency {

  /** Код валюты.
    */
  object CurrencyId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type CurrencyId = CurrencyId.Type

  implicit val circeDecoder: Decoder[Currency] = deriveDecoder
  implicit val avroCodec: Codec[Currency]      = Codec.derive[Currency]
}
