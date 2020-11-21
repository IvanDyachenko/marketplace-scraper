package marketplace.clients

import cats.Monad
import cats.syntax.all._
import cats.effect.{ConcurrentEffect, Resource, Sync}
import tofu.Execute
import tofu.lift.Unlift
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import org.http4s.{Method, Request => Http4sRequest, InvalidMessageBodyFailure}
import org.http4s.Status.Successful
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.client.dsl.Http4sClientDsl._
import org.http4s.client.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.asynchttpclient.proxy.ProxyServer
import org.http4s.circe.jsonEncoderOf

import marketplace.config.HttpConfig
import marketplace.models.{Request, Response}

@derive(representableK)
trait HttpClient[F[_]] {
  def send(request: Request): F[Response]
}

object HttpClient extends ContextEmbed[HttpClient] {
  def apply[F[_]](implicit ev: HttpClient[F]): ev.type = ev

  def make[I[_]: Monad: Execute: ConcurrentEffect: Unlift[*[_], F], F[_]: Sync](httpConfig: HttpConfig)(implicit
    logs: Logs[I, F]
  ): Resource[I, HttpClient[F]] =
    fromHttp4sClient[I](httpConfig).flatMap { http4sClient =>
      Resource.liftF(logs.forService[HttpClient[F]].map(implicit l => new Impl[F](translateHttp4sClient[I, F](http4sClient))))
    }

  class Impl[F[_]: Sync: Logging](http4sClient: Client[F]) extends HttpClient[F] {

    def send(request: Request): F[Response] =
      buildHttp4sRequest(request).flatMap { http4sRequest =>
        http4sClient
          .toKleisli { http4sResponse =>
            http4sResponse match {
              case Successful(_) =>
                http4sResponse.attemptAs[String].map(bodyText => Response(http4sResponse.headers, bodyText)).leftWiden[Throwable].rethrowT
              case unexpected    =>
                error"Received unexpected ${unexpected.status.show}-status during execution of request to ${request}" *>
                  UnexpectedStatus(unexpected.status).raiseError[F, Response]
            }
          }
          .run(http4sRequest)
          .recoverWith { case err: InvalidMessageBodyFailure =>
            errorCause"Received invalid response during execution of request to ${request}" (err) *>
              HttpClientDecodingError(err.details.dropWhile(_ != '{')).raiseError[F, Response]
          }
          .flatTap(response => debug"Received ${response} during execution of request to ${request}")
      }

    private def buildHttp4sRequest(request: Request): F[Http4sRequest[F]] =
      Method.POST(request, request.uri, request.headers.toList: _*)(Sync[F], jsonEncoderOf(Request.circeEncoder))
  }

  private def fromHttp4sClient[F[_]: Monad: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
    val HttpConfig(proxyHost, proxyPort, maxConnections, maxConnectionsPerHost) = httpConfig

    val proxyServer = new ProxyServer.Builder(proxyHost, proxyPort).build()

    val httpClientConfig = Dsl
      .config()
      .setMaxConnections(maxConnections)
      .setMaxConnectionsPerHost(maxConnectionsPerHost)
      .setFollowRedirect(false)
      .setKeepAlive(true)
      .setMaxRequestRetry(0)
      .setProxyServer(proxyServer)
      .build()

    AsyncHttpClient.resource(httpClientConfig)
  }

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))

//  // Should be fixed in https://github.com/TinkoffCreditSystems/tofu/pull/422
//  private object Execute {
//    def apply[F[_]](implicit F: Execute[F]): F.type = F
//  }

//  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect]: Resource[F, Client[F]] =
//    Resource.liftF(Execute[F].executionContext) >>= (BlazeClientBuilder[F](_).resource)
}
