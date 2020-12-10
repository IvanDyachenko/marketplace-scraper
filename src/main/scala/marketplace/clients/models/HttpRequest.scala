package marketplace.clients.models

import cats.implicits._
import cats.Inject
import vulcan.Codec
import io.circe.syntax._
import io.circe.Encoder
import tofu.logging.{DictLoggable, LogRenderer, Loggable}
import tofu.syntax.logRenderer._
import org.http4s.{Headers, Uri}

trait HttpRequest[+R] {
  def headers: Headers
  def uri: Uri
  def body: R
}

object HttpRequest {
  implicit def circeEncoder[R: Encoder]: Encoder[HttpRequest[R]]             = Encoder.instance(_.body.asJson)
  implicit def vulcanCodec[R: Inject[*, Array[Byte]]]: Codec[HttpRequest[R]] = ???

  implicit def loggable[Req]: Loggable[HttpRequest[Req]] = new DictLoggable[HttpRequest[Req]] {
    def logShow(a: HttpRequest[Req]): String = s"{a.uri.host}/${a.uri.path.show}"

    def fields[I, V, R, S](a: HttpRequest[Req], i: I)(implicit r: LogRenderer[I, V, R, S]): R =
      i.field("uri", a.uri.renderString) |+|
        a.uri.host.map(host => i.field("host", host.renderString)).getOrElse(i.noop) |+|
        i.field("path", a.uri.path.show) |+|
        i.foldTop(a.headers.toList)(h => i.addString(h.name.value, h.value)) |+|
        i.foldTop(a.uri.query.toList)(q => i.addString(q._1, q._2.getOrElse("")))
  }
}
