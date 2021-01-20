package marketplace.models.ozon

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic._

@derive(loggable, decoder)
@AvroNamespace("ozon.models")
case class ErrorDescr(error: String)

object ErrorDescr {
  implicit val vulcanCodec: Codec[ErrorDescr] = Codec.derive[ErrorDescr]
}
