package net.dalytics

import io.circe.Encoder
import org.http4s.{Headers, Uri, MediaType, Method, Request => Http4sRequest, ContentCoding, HttpVersion}
import org.http4s.circe.jsonEncoderOf
import org.http4s.headers.{`Accept-Encoding`, `User-Agent`, Accept, AgentComment, Connection, Host}

import net.dalytics.models.ozon.{Request => OzonRequest}
import net.dalytics.models.wildberries.{Request => WildBerriesRequest}
import net.dalytics.models.yandex.market.headers._
import net.dalytics.models.yandex.market.{Request => YandexMarketRequest}
import net.dalytics.models.yandex.market.Request.{Fields, RearrFactors, Sections}
import org.http4s.ProductId
import org.typelevel.ci.CIString

package object marshalling {

  implicit def ozonRequest2http4sRequest[F[_]](request: OzonRequest): Http4sRequest[F] = {
    val host: Uri.Host = Uri.RegName(request.host)

    val headers: Headers =
      Headers(
        Host(host.value),
        Connection(CaseInsensitiveString("Keep-Alive")),
        Accept(MediaType.application.json),
        `User-Agent`(AgentProduct("OzonStore", Some("463")))
      ) CIStringProductId

    val uri: Uri = {
      val uri = Uri(Some(Uri.Scheme.https), Some(Uri.Authority(host = host)))
        .addPath(request.path)
        .+*?(request.url)
        .+??(request.layoutContainer)
        .+??(request.layoutPageIndex)
        .+??(request.page)
        .+??(request.soldOutPage)

      request.searchFilters.foldLeft(uri)((uri, searchFilter) => uri.+?(searchFilter.key, searchFilter))
    }

    request match {
      case _: OzonRequest.GetCategorySearchFilterValues =>
        Http4sRequest(
          httpVersion = HttpVersion.`HTTP/1.1`,
          method = Method.PUT,
          uri = uri,
          headers = headers
        ).withEntity(request)(jsonEncoderOf[F, OzonRequest])
      case _                                            =>
        Http4sRequest(
          httpVersion = HttpVersion.`HTTP/1.1`,
          method = Method.GET,
          uri = uri,
          headers = headers
        ).withEmptyBody
    }
  }

  implicit def wildBerriesRequest2http4sRequest[F[_]](request: WildBerriesRequest): Http4sRequest[F] = {
    val host: Uri.Host = Uri.RegName(request.host)

    val headers: Headers =
      Headers(
        Host(host.value),
        Connection(CaseInsensitiveString("Keep-Alive")),
        Accept(MediaType.application.json),
        `Accept-Encoding`(ContentCoding.gzip),
        `User-Agent`(
          AgentProduct("Wildberries", Some("3.3.1000")),
          List(AgentComment("RU.WILDBERRIES.MOBILEAPP; build:1433770; iOS 14.4.0"), AgentProduct("Alamofire", Some("5.2.2")))
        )
      ) CIStringProductIdProductId

    val uri: Uri =
      Uri(Some(Uri.Scheme.https), Some(Uri.Authority(host = host)))
        .withPath(request.path)
        .+*?(request.lang)
        .+*?(request.locale)

    Http4sRequest[F](
      method = Method.GET,
      uri = uri,
      headers = headers
    )
  }

  implicit def yandexMarketRequest2http4sRequest[F[_]](request: YandexMarketRequest): Http4sRequest[F] = {
    val host: Uri.Host = Uri.RegName(request.host)

    val headers: Headers =
      Headers(
        Host(host.value),
        Connection(CaseInsensitiveString("Keep-Alive")),
        Accept(MediaType.application.json),
        `User-Agent`(AgentProduct("Beru", Some("330")), List(AgentComment("iPhone; iOS 14.2; Scale/3.00"))),
        `X-Device-Type`("SMARTPHONE"),
        `X-Platform`("IOS"),
        `X-App-Version`("3.3.0"),
        `X-Region-Id`(request.geoId)
      ) CIStringProductId

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
