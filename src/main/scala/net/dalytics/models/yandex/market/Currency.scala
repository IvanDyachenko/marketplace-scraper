package net.dalytics.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Валюта запроса.
  *
  * @param id   Код валюты.
  * @param name Название валюты.
  */
@derive(loggable, decoder)
case class Currency(id: Currency.CurrencyId, name: String)

object Currency {

  /** Код валюты.
    */
  object CurrencyId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce
  type CurrencyId = CurrencyId.Type
}
