package marketplace

import cats.effect.Sync
import io.circe.Encoder
import org.http4s.{Headers, Uri}
import org.http4s.{Method, Request => Http4sRequest}
import org.http4s.headers.{`User-Agent`, AgentComment, AgentProduct, Host}
import org.http4s.circe.jsonEncoderOf

import marketplace.models.yandex.market.headers._
import marketplace.models.yandex.market.{Request => YandexMarketRequest}
import marketplace.models.yandex.market.Request.{Fields, RearrFactors, Sections}

package object marshalling {

  implicit def yandexMarketRequest2http4sRequest[F[_]: Sync](request: YandexMarketRequest): Http4sRequest[F] = {
    val host: Uri.Host = Uri.RegName(request.host)

    val headers: Headers =
      Headers.of(
        Host(host.value),
        `User-Agent`(AgentProduct("Beru", Some("323")), List(AgentComment("iPhone; iOS 14.0.1; Scale/3.00"))),
        `X-Device-Type`("SMARTPHONE"),
        `X-Platform`("IOS"),
        `X-App-Version`("3.2.3"),
        `X-Region-Id`(request.geoId)
      )

    val uri: Uri =
      Uri(Some(Uri.Scheme.https), Some(Uri.Authority(host = host)))
        .addPath(request.method)
        .+*?(request.uuid)
        .+*?(request.geoId)
        .+??(request.page)
        .+??(request.count)
        .+*?[Fields](request.fields)
        .+*?[Sections](request.sections)
        .+*?[RearrFactors](request.rearrFactors)

    Http4sRequest(
      method = Method.POST,
      uri = uri,
      headers = headers
    ).withEntity(request)(jsonEncoderOf(Encoder[YandexMarketRequest]))
  }
}
