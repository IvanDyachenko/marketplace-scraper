package marketplace

import io.circe.Encoder
import org.http4s.{Headers, Uri, MediaType, Method, Request => Http4sRequest, ContentCoding, ProductId, ProductComment}
import org.http4s.headers.{`Accept-Encoding`, `User-Agent`, Accept, Host}
import org.http4s.circe.jsonEncoderOf

import marketplace.models.ozon.{Request => OzonRequest}

import marketplace.models.yandex.market.headers._
import marketplace.models.yandex.market.{Request => YandexMarketRequest}
import marketplace.models.yandex.market.Request.{Fields, RearrFactors, Sections}

package object marshalling {

  implicit def ozonRequest2http4sRequest[F[_]](request: OzonRequest): Http4sRequest[F] = {
    val host: Uri.Host = Uri.RegName(request.host)

    val headers: Headers =
      Headers.of(
        Host(host.value),
        Accept(MediaType.application.json),
        `User-Agent`(ProductId("OzonStore", Some("400")))
      )

    val uri: Uri =
      Uri(Some(Uri.Scheme.https), Some(Uri.Authority(host = host)))
        .addPath(request.path)
        .+*?(request.url)

    Http4sRequest(
      method = Method.GET,
      uri = uri,
      headers = headers
    )
  }

  implicit def yandexMarketRequest2http4sRequest[F[_]](request: YandexMarketRequest): Http4sRequest[F] = {
    val host: Uri.Host = Uri.RegName(request.host)

    val headers: Headers =
      Headers.of(
        Host(host.value),
        Accept(MediaType.application.json, MediaType.text.plain),
        `Accept-Encoding`(ContentCoding.deflate, ContentCoding.gzip),
        `User-Agent`(ProductId("Beru", Some("330")), List(ProductComment("iPhone; iOS 14.2; Scale/3.00"))),
        `X-Device-Type`("SMARTPHONE"),
        `X-Platform`("IOS"),
        `X-App-Version`("3.3.0"),
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
