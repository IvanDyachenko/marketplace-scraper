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
  def headers: Headers
  def path: Uri.Path
  def queryParams: Map[String, String]
}

object Request {
  implicit val loggable: Loggable[Request] = new DictLoggable[Request] {
    def fields[I, V, R, S](a: Request, i: I)(implicit r: LogRenderer[I, V, R, S]): R =
      i.field("uri", a.uri.renderString) |+| i.field("path", a.path.show) |+|
        i.foldTop(a.headers.toList)(h => i.addString(h.name.value, h.value))

    def logShow(a: Request): String = a.path.show
  }

  implicit val circeEncoder: Encoder[Request] = Encoder.instance { case yaMarketRequest: YaMarketRequest => yaMarketRequest.asJson }
}
