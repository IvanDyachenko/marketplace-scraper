package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

/** Валюта запроса.
  *
  * @param id   Код валюты.
  * @param name Название валюты.
  */
@derive(loggable)
case class Currency(id: Currency.CurrencyId, name: String)

object Currency {
  implicit val circeDecoder: Decoder[Currency] = deriveDecoder

  /** Код валюты.
    */
  object CurrencyId extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = Loggable.stringValue.contramap(identity)
    implicit val circeDecoder: Decoder[Type] = lift
  }
  type CurrencyId = CurrencyId.Type
}
