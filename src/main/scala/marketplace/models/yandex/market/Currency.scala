package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

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
  object CurrencyId extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val avroCodec: Codec[Type]      = lift
  }
  type CurrencyId = CurrencyId.Type

  implicit val circeDecoder: Decoder[Currency] = deriveDecoder
  implicit val avroCodec: Codec[Currency]      = Codec.derive[Currency]
}
