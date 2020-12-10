package marketplace.clients.models

import cats.Inject
import vulcan.Codec
import tofu.logging.Loggable
import org.http4s.Headers

final case class HttpResponse[+R](headers: Headers, result: R)

object HttpResponse {
  implicit def vulcanCodec[R: Inject[*, Array[Byte]]]: Codec[HttpResponse[R]] = ???

  implicit def loggable[R]: Loggable[HttpResponse[R]] = Loggable.stringValue.contramap(_ => "response")
}
