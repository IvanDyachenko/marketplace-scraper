package marketplace.models

import org.http4s.Headers
import tofu.logging.Loggable

final case class Response(headers: Headers, bodyText: String)

object Response {
  implicit val loggable: Loggable[Response] = Loggable.stringValue.contramap(resp => "'response'")
}
