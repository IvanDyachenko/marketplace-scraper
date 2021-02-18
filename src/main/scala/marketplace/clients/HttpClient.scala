package marketplace.clients

import java.util.concurrent.TimeoutException
import scala.util.control.NoStackTrace

import cats.implicits._
import cats.{FlatMap, Monad}
import cats.effect.{ConcurrentEffect, Resource, Sync}
import tofu.syntax.raise._
import tofu.syntax.logging._
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.{Execute, Handle, Raise}
import tofu.lift.Unlift
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.logging.{Logging, Logs}
import io.circe.{Decoder, DecodingFailure}
import org.http4s.{Request => Http4sRequest, Status}
import org.http4s.circe.jsonOf
import org.http4s.client.Client
//import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.asynchttpclient.proxy.ProxyServer

import marketplace.config.HttpConfig

trait HttpClient[F[_]] {
  def send[Res: Decoder](request: Http4sRequest[F]): F[Res]
}

@derive(loggable)
sealed trait HttpClientError extends NoStackTrace

object HttpClientError {
  @derive(loggable)
  final case class DecodingError(message: String) extends HttpClientError

  @derive(loggable)
  final case class TimeoutException(message: String) extends HttpClientError

  @derive(loggable)
  final case class UnexpectedStatus(message: String) extends HttpClientError
}

object HttpClient extends ContextEmbed[HttpClient] {
  type Raising[F[_]]  = Raise[F, HttpClientError]
  type Handling[F[_]] = Handle[F, HttpClientError]

  class Impl[F[_]: Sync: Logging: Raising](http4sClient: Client[F]) extends HttpClient[F] {

    def send[Res](request: Http4sRequest[F])(implicit decoder: Decoder[Res]): F[Res] =
      http4sClient
        .toKleisli { response =>
          response match {
            case Status.Successful(_) =>
              jsonOf(Sync[F], decoder)
                .decode(response, strict = false)
                .rethrowT
            case unexpected           =>
              val message     = s"Received ${unexpected.status.code} status during execution of the request to ${request.uri.show}"
              val clientError = HttpClientError.UnexpectedStatus(message)
              error"${message}" *> clientError.raise[F, Res]
          }
        }
        .run(request)
        .recoverWith {
          case error: TimeoutException =>
            val message = error.getMessage
            error"${message}" *> HttpClientError.TimeoutException(message).raise[F, Res]
          case error: DecodingFailure  =>
            val message =
              s"A response received as a result of the request to ${request.uri.show} was rejected because of a decoding failure: ${error.show}"
            error"${message}" *> HttpClientError.DecodingError(message).raise[F, Res]
          // case error: MessageFailure   =>
          //   val message =
          //     s"A response received as a result of the request to ${request.uri.show} was rejected because of a decoding failure: ${error.message.take(120)}"
          //   //.takeWhile(_ != '{')}"
          //   error"${message}" *> HttpClientError.DecodingError(message).raise[F, Res]
        }
  }

  implicit val embed: Embed[HttpClient] = new Embed[HttpClient] {
    def embed[F[_]: FlatMap](ft: F[HttpClient[F]]): HttpClient[F] = new HttpClient[F] {
      def send[Res: Decoder](request: Http4sRequest[F]): F[Res] = ft >>= (_.send(request))
    }
  }

  def apply[F[_]](implicit ev: HttpClient[F]): ev.type = ev

  def make[
    I[_]: Monad: Execute: ConcurrentEffect: Unlift[*[_], F],
    F[_]: Sync: Raising
  ](httpConfig: HttpConfig)(implicit logs: Logs[I, F]): Resource[I, HttpClient[F]] =
    buildHttp4sClient[I](httpConfig) >>= { http4sClient =>
      Resource.liftF(logs.forService[HttpClient[F]].map(implicit l => new Impl[F](translateHttp4sClient[I, F](http4sClient))))
    }

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))

  private def buildHttp4sClient[F[_]: Monad: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
    val HttpConfig(proxyHost, proxyPort, requestTimeout, maxConnections, maxConnectionsPerHost) = httpConfig

    val proxyServer = new ProxyServer.Builder(proxyHost, proxyPort).build()

    val httpClientConfig = Dsl
      .config()
      .setMaxConnections(maxConnections)
      .setMaxConnectionsPerHost(maxConnectionsPerHost)
      .setFollowRedirect(false)
      .setKeepAlive(true)
      .setMaxRequestRetry(3)
      .setRequestTimeout(requestTimeout.toMillis.toInt)
      .setProxyServer(proxyServer)
      .build()

    AsyncHttpClient.resource(httpClientConfig)
  }

  //  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] =
  //     Resource.liftF(Execute[F].executionContext) >>= (BlazeClientBuilder[F](_).withDefaultSslContext
  //       .withRequestTimeout(httpConfig.requestTimeout)
  //       .withMaxTotalConnections(httpConfig.maxConnections)
  //       .withMaxConnectionsPerRequestKey(_ => httpConfig.maxConnectionsPerHost)
  //       .resource)
}
