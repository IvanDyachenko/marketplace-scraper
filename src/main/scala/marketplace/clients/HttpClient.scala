package marketplace.clients

import scala.util.control.NoStackTrace

import cats.syntax.show._
import tofu.syntax.raise._
import tofu.syntax.handle._
import tofu.syntax.monadic._
import derevo.derive
import tofu.logging.derivation.loggable
import cats.{FlatMap, Monad}
import cats.effect.{ConcurrentEffect, Resource, Sync}
import tofu.{Execute, Handle, Raise}
import tofu.lift.Unlift
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.logging.{Logging, Logs}
import io.circe.{Decoder, DecodingFailure}
import org.http4s.{Request => Http4sRequest, Status}
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.armeria.client.ArmeriaClientBuilder
import com.linecorp.armeria.client.{ResponseTimeoutException, WebClient}
import com.linecorp.armeria.client.retry.{Backoff, RetryRule, RetryingClient}
import com.linecorp.armeria.client.logging.LoggingClient
import com.linecorp.armeria.common.{HttpMethod, HttpStatus}
//import org.http4s.client.blaze.BlazeClientBuilder
//import org.asynchttpclient.Dsl
//import org.http4s.client.asynchttpclient.AsyncHttpClient

import marketplace.config.HttpConfig

trait HttpClient[F[_]] {
  def send[Res: Decoder](request: Http4sRequest[F]): F[Res]
}

@derive(loggable)
sealed trait HttpClientError extends NoStackTrace {
  def message: String
}

object HttpClientError {
  @derive(loggable)
  final case class DecodingError(message: String) extends HttpClientError

  @derive(loggable)
  final case class ResponseTimeoutError(message: String) extends HttpClientError

  @derive(loggable)
  final case class UnexpectedStatusError(message: String) extends HttpClientError
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
              HttpClientError
                .UnexpectedStatusError(s"Received ${unexpected.status.code} status during execution of the request to ${request.uri.show}")
                .raise[F, Res]
          }
        }
        .run(request)
        .recoverWith[RuntimeException] { case error: RuntimeException =>
          HttpClientError
            .ResponseTimeoutError(error.toString())
            .raise[F, Res]
        }
        .recoverWith[DecodingFailure] { case error: DecodingFailure =>
          HttpClientError
            .DecodingError(
              s"A response received as a result of the request to ${request.uri.show} was rejected because of a decoding failure: ${error.show}"
            )
            .raise[F, Res]
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

  private def buildHttp4sClient[F[_]: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
    val backoff: Backoff =
      Backoff
        .withoutDelay()
        .withMaxAttempts(httpConfig.backoffMaxAttempts)

    val retryRule: RetryRule =
      RetryRule.of(
        RetryRule
          .builder()
          .onStatus(HttpStatus.TOO_MANY_REQUESTS)
          .thenNoRetry(),
        RetryRule
          .builder(HttpMethod.idempotentMethods)
          .onServerErrorStatus()
          .onException(classOf[RuntimeException])
          .onException(classOf[ResponseTimeoutException])
          .thenBackoff(backoff)
      )

    ArmeriaClientBuilder(WebClient.builder())
      .withResponseTimeout(httpConfig.responseTimeout)
      .withDecorator(RetryingClient.newDecorator(retryRule))
      .withDecorator(LoggingClient.newDecorator())
      .resource
  }

  // private def buildHttp4sClient[F[_]: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
  //   val HttpConfig(_, _, _, responseTimeout, maxTotalConnections, maxConnectionsPerHost) = httpConfig

  //   val httpClientConfig = Dsl
  //     .config()
  //     .setPooledConnectionIdleTimeout(500)
  //     .setMaxConnections(maxTotalConnections)
  //     .setMaxConnectionsPerHost(maxConnectionsPerHost)
  //     .setFollowRedirect(false)
  //     .setRequestTimeout(responseTimeout.toMillis.toInt)
  //     .build()

  //   AsyncHttpClient.resource(httpClientConfig)
  // }

  // private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] =
  //   Resource.liftF(Execute[F].executionContext) >>= (BlazeClientBuilder[F](_)
  //     .withRequestTimeout(httpConfig.requestTimeout)
  //     .withMaxTotalConnections(httpConfig.maxTotalConnections)
  //     .withMaxConnectionsPerRequestKey(Function.const(httpConfig.maxConnectionsPerHost))
  //     .resource)
}
