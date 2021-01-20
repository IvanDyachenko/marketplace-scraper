package marketplace.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable

/** Сообщение об ошибке.
  *
  * @param message Описание ошибки.
  */
@derive(loggable, decoder)
case class ErrorDescr(message: String)
