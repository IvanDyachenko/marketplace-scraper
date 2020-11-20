package marketplace.models

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Encoder

@derive(loggable)
final case class Request(uri: String, headers: Headers, path: Path, queryParams: Map[String, Seq[String]])

object Request {
  implicit val circeEncoder: Encoder[Request] = ???
}
