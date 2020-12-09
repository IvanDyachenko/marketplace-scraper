package marketplace.clients.models

import tofu.logging.Loggable
import org.http4s.Headers

final case class HttpResponse[+R](headers: Headers, result: R)

object HttpResponse {
  implicit def loggable[R]: Loggable[HttpResponse[R]] = Loggable.stringValue.contramap(_ => "response")
}
