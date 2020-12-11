package marketplace

import org.http4s.{Headers, Uri}
import org.http4s.headers.{`User-Agent`, AgentComment, AgentProduct, Host}

import marketplace.clients.models.HttpRequest
import marketplace.models.yandex.market.headers._
import marketplace.models.yandex.market.{Request => YandexMarketRequest}
import marketplace.models.yandex.market.Request.{Fields, RearrFactors, Sections}

package object marshalling {

  implicit class YandexMarketRequestMarshaller(val request: YandexMarketRequest) {

    def toHttpRequest: HttpRequest[YandexMarketRequest] = new HttpRequest[YandexMarketRequest] {
      private val host: Uri.Host = Uri.RegName(request.host)

      def uri: Uri =
        Uri(Some(Uri.Scheme.https), Some(Uri.Authority(host = host)))
          .addPath(request.method)
          .+*?(request.uuid)
          .+*?(request.geoId)
          .+??(request.page)
          .+??(request.count)
          .+*?[Fields](request.fields)
          .+*?[Sections](request.sections)
          .+*?[RearrFactors](request.rearrFactors)

      def headers: Headers =
        Headers.of(
          Host(host.value),
          `User-Agent`(AgentProduct("Beru", Some("323")), List(AgentComment("iPhone; iOS 14.0.1; Scale/3.00"))),
          `X-Device-Type`("SMARTPHONE"),
          `X-Platform`("IOS"),
          `X-App-Version`("3.2.3"),
          `X-Region-Id`(request.geoId)
        )

      def body: YandexMarketRequest = request
    }
  }
}
