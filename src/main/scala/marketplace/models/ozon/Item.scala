package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import io.circe.Decoder
import io.circe.derivation.deriveDecoder

@derive(loggable)
@AvroNamespace("ozon.models")
final case class Item(
  link: String
)

object Item {
  implicit val circeDecoder: Decoder[Item] = deriveDecoder
  implicit val avroCodec: Codec[Item]      = Codec.derive[Item]
}
