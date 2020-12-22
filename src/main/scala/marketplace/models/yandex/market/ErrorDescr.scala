package marketplace.models.yandex.market

import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

/** Сообщение об ошибке.
  *
  * @param message Описание ошибки.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
case class ErrorDescr(message: String)

object ErrorDescr {
  implicit val circeDecoder: Decoder[ErrorDescr] = deriveDecoder
  implicit val avroCodec: Codec[ErrorDescr]      = Codec.derive[ErrorDescr]
}
