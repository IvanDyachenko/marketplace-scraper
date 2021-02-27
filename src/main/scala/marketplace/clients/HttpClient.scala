package marketplace.clients

import java.util.concurrent.TimeoutException
import scala.util.control.NoStackTrace
import scala.concurrent.duration.Duration

import cats.syntax.show._
import tofu.syntax.raise._
import tofu.syntax.handle._
import tofu.syntax.monadic._
import derevo.derive
import tofu.logging.derivation.loggable
import cats.{FlatMap, Monad}
import cats.effect.{ConcurrentEffect, Resource, Sync, Timer}
import tofu.{Execute, Handle, Raise}
import tofu.lift.Unlift
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.logging.{Logging, Logs}
import io.circe.Decoder
import org.http4s.{DecodeFailure, Request => Http4sRequest, Status}
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.client.middleware.{GZip, Retry, RetryPolicy}
import org.http4s.client.blaze.BlazeClientBuilder
//import org.http4s.armeria.client.ArmeriaClientBuilder
//import com.linecorp.armeria.client.{ClientFactory, ResponseTimeoutException, WebClient}
//import com.linecorp.armeria.client.retry.{RetryConfig, RetryRule, RetryingClient}
//import com.linecorp.armeria.common.util.EventLoopGroups
//import com.linecorp.armeria.client.proxy.ProxyConfig
//import com.linecorp.armeria.client.logging.LoggingClient
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
  final case class ResponseDecodeError(message: String) extends HttpClientError

  @derive(loggable)
  final case class ResponseTimeoutError(message: String) extends HttpClientError

  @derive(loggable)
  final case class ResponseUnexpectedStatusError(message: String) extends HttpClientError
}

object HttpClient extends ContextEmbed[HttpClient] {
  type Raising[F[_]]  = Raise[F, HttpClientError]
  type Handling[F[_]] = Handle[F, HttpClientError]

  class Impl[F[_]: Sync: Logging: Raising: Handle[*[_], TimeoutException]: Handle[*[_], DecodeFailure]](http4sClient: Client[F])
      extends HttpClient[F] {

    def send[Res](request: Http4sRequest[F])(implicit decoder: Decoder[Res]): F[Res] =
      http4sClient
        .run(request)
        .use { response =>
          response match {
            case Status.Successful(_) =>
              jsonOf(Sync[F], decoder)
                .decode(response, strict = false)
                .rethrowT

            case unexpected =>
              HttpClientError
                .ResponseUnexpectedStatusError(s"Received ${unexpected.status.code} status during execution of the request to ${request.uri.show}")
                .raise[F, Res]
          }
        }
        .recoverWith[TimeoutException] { error =>
          HttpClientError
            .ResponseTimeoutError(error.toString)
            .raise[F, Res]
        }
        .recoverWith[DecodeFailure] { error =>
          val errorDetails = error.cause.fold(error.message.takeWhile(_ != '{'))(_.getMessage)

          HttpClientError
            .ResponseDecodeError(
              s"A response received as a result of the request to ${request.uri.show} was rejected because of a decoding failure. ${errorDetails}"
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
    I[_]: Monad: Timer: Execute: ConcurrentEffect: Unlift[*[_], F],
    F[_]: Sync: Raising
  ](httpConfig: HttpConfig)(implicit logs: Logs[I, F]): Resource[I, HttpClient[F]] =
    buildHttp4sClient[I](httpConfig) >>= { http4sClient =>
      Resource.liftF(logs.forService[HttpClient[F]].map(implicit l => new Impl[F](translateHttp4sClient[I, F](http4sClient))))
    }

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))

  // private def buildHttp4sClient[F[_]: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
  //   val retryRule: RetryRule =
  //     RetryRule
  //       .failsafe()
  //
  //   val retryConfig =
  //     RetryConfig
  //       .builder(retryRule)
  //       .maxTotalAttempts(httpConfig.maxTotalAttempts)
  //       .responseTimeoutMillisForEachAttempt(httpConfig.responseTimeoutForEachAttempt.toMillis)
  //       .build()
  //
  //   val clientFactory: ClientFactory =
  //     ClientFactory
  //       .builder()
  //       .tlsNoVerify()
  //       .useHttp2Preface(false)
  //       .useHttp1Pipelining(false)
  //     //.workerGroup(EventLoopGroups.newEventLoopGroup(httpConfig.eventLoopGroupNumThreads, "armeria-event-loop-groups"), true)
  //       .maxNumRequestsPerConnection(httpConfig.maxNumRequestsPerConnection)
  //       .build()
  //
  //   val webClient =
  //     WebClient
  //       .builder()
  //       .factory(clientFactory)
  //       .responseTimeoutMillis(httpConfig.responseTimeout.toMillis)
  //       .decorator(RetryingClient.newDecorator(retryConfig))
  //     //.decorator(LoggingClient.newDecorator())
  //
  //   ArmeriaClientBuilder(webClient).resource
  // }

  // private def buildHttp4sClient[F[_]: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] =
  //   AsyncHttpClient.resource {
  //     Dsl
  //       .config()
  //       .setMaxConnections(httpConfig.maxTotalConnections)
  //       .setMaxConnectionsPerHost(httpConfig.maxNumConnectionsPerHost)
  //       .setFollowRedirect(false)
  //       //.setMaxRequestRetry(httpConfig.maxTotalAttempts)
  //       .setRequestTimeout(httpConfig.responseTimeoutForEachAttempt.toMillis.toInt)
  //       .build()
  //   }

  private def buildHttp4sClient[F[_]: Timer: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] =
    Resource.liftF(Execute[F].executionContext) >>= (
      BlazeClientBuilder[F](_)
        .withRequestTimeout(httpConfig.responseTimeoutForEachAttempt)
        .withMaxTotalConnections(httpConfig.maxTotalConnections)
        .withMaxConnectionsPerRequestKey(Function.const(httpConfig.maxNumConnectionsPerHost))
        .resource
        .map(GZip())
        .map(Retry(recklesslyRetryPolicy(httpConfig.responseTimeoutForEachAttempt, httpConfig.maxTotalAttempts)))
    )

  private def recklesslyRetryPolicy[F[_]](maxWait: Duration, maxRetry: Int): RetryPolicy[F] =
    RetryPolicy(RetryPolicy.exponentialBackoff(maxWait = maxWait, maxRetry = maxRetry), (_, result) => RetryPolicy.recklesslyRetriable(result))
}
