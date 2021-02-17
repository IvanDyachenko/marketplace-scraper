package marketplace.models.wildberries

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder

@derive(loggable)
final case class Url(
  path: String,
  query: String
)

object Url {
  implicit val circeDecoder: Decoder[Url] = Decoder.forProduct2("pageUrl", "query")(apply)
}
