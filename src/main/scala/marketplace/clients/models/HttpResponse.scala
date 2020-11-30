package marketplace.clients.models

import tofu.logging.Loggable
import org.http4s.Headers
import io.circe.{Decoder, Json}
import doobie.util.meta.Meta

final case class HttpResponse[R](headers: Headers, result: R)

object HttpResponse {
  implicit def loggable[R]: Loggable[HttpResponse[R]] = Loggable.stringValue.contramap(_ => "response")

  implicit val jsonDoobieMeta: Meta[Json]      = ???
  implicit val jsonCirceDecoder: Decoder[Json] = ???
}
