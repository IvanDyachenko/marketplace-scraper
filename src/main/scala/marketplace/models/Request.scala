package marketplace.models

import cats.implicits._
import io.circe.syntax._
import io.circe.Encoder
import tofu.logging.{DictLoggable, LogRenderer, Loggable}
import tofu.syntax.logRenderer._
import org.http4s.{Headers, Uri}

import marketplace.models.yandex.market.{Request => YaMarketRequest}

trait Request {
  def uri: Uri
  def host: Uri.Host
  def path: Uri.Path
  def headers: Headers
  def queryParams: Map[String, String]
}

object Request {
  implicit val loggable: Loggable[Request] = new DictLoggable[Request] {
    def logShow(a: Request): String = s"{a.host}/${a.path.show}"

    def fields[I, V, R, S](a: Request, i: I)(implicit r: LogRenderer[I, V, R, S]): R =
      i.field("uri", a.uri.renderString) |+|
        i.field("host", a.host.renderString) |+|
        i.field("path", a.path.show) |+|
        i.foldTop(a.headers.toList)(h => i.addString(h.name.value, h.value)) |+|
        i.foldTop(a.queryParams.toList)(q => i.addString(q._1, q._2))
  }

  implicit val circeEncoder: Encoder[Request] = Encoder.instance { case yaMarketRequest: YaMarketRequest => yaMarketRequest.asJson }
}
