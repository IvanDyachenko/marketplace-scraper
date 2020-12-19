package marketplace.clients

import cats.{FlatMap, Monad}
import cats.syntax.all._
import cats.effect.{ConcurrentEffect, Resource, Sync}
import tofu.Execute
import tofu.lift.Unlift
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import io.circe.Decoder
import org.http4s.{Request => Http4sRequest, InvalidMessageBodyFailure}
import org.http4s.Status.Successful
import org.http4s.circe.jsonOf
import org.http4s.client.{Client, UnexpectedStatus}
import org.http4s.client.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.asynchttpclient.proxy.ProxyServer

import marketplace.config.HttpConfig
import marketplace.clients.models.HttpClientDecodingError

trait HttpClient[F[_]] {
  def send[Res: Decoder](request: Http4sRequest[F]): F[Res]
}

object HttpClient extends ContextEmbed[HttpClient] {

  class Impl[F[+_]: Sync: Logging](http4sClient: Client[F]) extends HttpClient[F] {

    def send[Res](request: Http4sRequest[F])(implicit decoder: Decoder[Res]): F[Res] =
      http4sClient
        .toKleisli { response =>
          response match {
            case Successful(_) =>
              jsonOf(Sync[F], decoder)
                .decode(response, strict = false)
                .leftWiden[Throwable]
                .rethrowT
            case unexpected    =>
              error"Received ${unexpected.status.show} status during execution of request to ${request.uri.path}" *>
                UnexpectedStatus(unexpected.status).raiseError[F, Res]
          }
        }
        .run(request)
        .recoverWith { case err: InvalidMessageBodyFailure =>
          errorCause"Received invalid response during execution of request to ${request.uri.path}" (err) *>
            HttpClientDecodingError(err.details.dropWhile(_ != '{')).raiseError[F, Res]
        }
        .flatTap(response => debug"Received ... during execution of request to ${request.uri.path}")
  }

  def apply[F[_]](implicit ev: HttpClient[F]): ev.type = ev

  implicit val embed: Embed[HttpClient] = new Embed[HttpClient] {
    def embed[F[_]: FlatMap](ft: F[HttpClient[F]]): HttpClient[F] = new HttpClient[F] {
      def send[Res: Decoder](request: Http4sRequest[F]): F[Res] = ft >>= (_.send(request))
    }
  }

  def make[I[_]: Monad: Execute: ConcurrentEffect: Unlift[*[_], F], F[+_]: Sync](httpConfig: HttpConfig)(implicit
    logs: Logs[I, F]
  ): Resource[I, HttpClient[F]] =
    buildHttp4sClient[I](httpConfig) >>= { http4sClient =>
      Resource.liftF(logs.forService[HttpClient[F]].map(implicit l => new Impl[F](translateHttp4sClient[I, F](http4sClient))))
    }

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))

  private def buildHttp4sClient[F[_]: Monad: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
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

//  // Should be fixed in https://github.com/TinkoffCreditSystems/tofu/pull/422
//  private object Execute {
//    def apply[F[_]](implicit F: Execute[F]): F.type = F
//  }

//  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect]: Resource[F, Client[F]] =
//    Resource.liftF(Execute[F].executionContext) >>= (BlazeClientBuilder[F](_).resource)
}
