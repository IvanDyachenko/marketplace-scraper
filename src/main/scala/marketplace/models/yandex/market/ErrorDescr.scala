package marketplace.models.yandex.market

import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.derivation.loggable

/** Сообщение об ошибке.
  *
  * @param message Описание ошибки.
  */
@derive(loggable)
case class ErrorDescr(message: String)

object ErrorDescr {
  implicit val circeDecoder: Decoder[ErrorDescr] = deriveDecoder
}
