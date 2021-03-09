package net.dalytics.clients

import java.util.concurrent.TimeoutException
//import java.net.http.{HttpClient => jdkHttpClient}

import scala.util.control.NoStackTrace
import scala.concurrent.duration._

import cats.syntax.show._
import tofu.syntax.raise._
import tofu.syntax.handle._
import tofu.syntax.monadic._
import derevo.derive
import tofu.logging.derivation.loggable
import cats.FlatMap
import cats.effect.{ConcurrentEffect, Resource, Sync, Timer}
import tofu.{Execute, Handle, Raise}
import tofu.lift.Unlift
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.logging.Logs
import io.circe.Decoder
import org.http4s.{DecodeFailure, Request => Http4sRequest, Status}
import org.http4s.circe.jsonOf
import org.http4s.client.{Client, ConnectionFailure}
//import org.http4s.client.jdkhttpclient.JdkHttpClient
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{GZip, Retry, RetryPolicy}

import net.dalytics.config.HttpConfig

trait HttpClient[F[_]] {
  def send[Res: Decoder](request: Http4sRequest[F]): F[Res]
}

@derive(loggable)
sealed trait HttpClientError extends NoStackTrace {
  def message: String
}

object HttpClientError {
  @derive(loggable)
  final case class ConnectTimeoutError(message: String) extends HttpClientError

  @derive(loggable)
  final case class RequestTimeoutError(message: String) extends HttpClientError

  @derive(loggable)
  final case class ResponseDecodeError(message: String) extends HttpClientError

  @derive(loggable)
  final case class ResponseUnexpectedStatusError(message: String) extends HttpClientError
}

object HttpClient extends ContextEmbed[HttpClient] {
  type Raising[F[_]]  = Raise[F, HttpClientError]
  type Handling[F[_]] = Handle[F, HttpClientError]

  class Impl[F[_]: Sync: Handle[*[_], TimeoutException]: Handle[*[_], DecodeFailure]](http4sClient: Client[F]) extends HttpClient[F] {

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
        .recoverWith[ConnectionFailure] { error =>
          HttpClientError
            .ConnectTimeoutError(error.toString)
            .raise[F, Res]
        }
        .recoverWith[TimeoutException] { error =>
          HttpClientError
            .RequestTimeoutError(error.toString)
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
    I[_]: Execute: ConcurrentEffect: Timer: Unlift[*[_], F],
    F[_]: Sync
  ](httpConfig: HttpConfig)(implicit logs: Logs[I, F]): Resource[I, HttpClient[F]] =
    for {
      http4sClient <- buildHttp4sClient[I](httpConfig)
                        .map(GZip())
                        .map { http4sClient =>
                          val retryPolicy = recklesslyRetryPolicy[I](httpConfig.requestMaxDelayBetweenAttempts, httpConfig.requestMaxTotalAttempts)
                          Retry(retryPolicy)(http4sClient)
                        }
      httpClient   <- Resource.liftF(logs.forService[HttpClient[F]].map(_ => new Impl[F](translateHttp4sClient[I, F](http4sClient))))
    } yield httpClient

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_], G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))

  // private def buildHttp4sClient[F[_]: ContextShift: ConcurrentEffect](blocker: Blocker, httpConfig: HttpConfig): Resource[F, Client[F]] = {
  //   def blockerExecutor(blocker: Blocker): Executor = new Executor {
  //     override def execute(command: Runnable): Unit = blocker.blockingContext.execute(command)
  //   }
  //
  //   Resource.liftF(
  //     Sync[F].delay {
  //       val httpClient = jdkHttpClient
  //         .newBuilder()
  //         .executor(blockerExecutor(blocker))
  //         //.proxy(ProxySelector.of(new InetSocketAddress(httpConfig.proxyHost, httpConfig.proxyPort)))
  //         .version(jdkHttpClient.Version.HTTP_1_1)
  //         .followRedirects(jdkHttpClient.Redirect.NEVER)
  //         .connectTimeout(java.time.Duration.ofNanos(httpConfig.connectTimeout.toNanos))
  //         .build()
  //
  //       JdkHttpClient[F](httpClient)
  //     }
  //   )
  // }

  // private def buildHttp4sClient[F[_]: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] =
  //   AsyncHttpClient.resource {
  //     Dsl
  //       .config()
  //       .setMaxConnections(httpConfig.maxTotalConnections)
  //       .setMaxConnectionsPerHost(httpConfig.maxTotalConnectionsPerHost)
  //       .setMaxRequestRetry(httpConfig.requestMaxTotalAttempts)
  //       .setRequestTimeout(httpConfig.requestTimeout.toMillis.toInt)
  //       .setFollowRedirect(false)
  //       .build()
  //   }

  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] =
    Resource.liftF(Execute[F].executionContext) >>= (
      BlazeClientBuilder[F](_)
        .withTcpNoDelay(true) // Disable Nagle's algorithm.
        .withCheckEndpointAuthentication(false)
        .withMaxTotalConnections(httpConfig.maxTotalConnections)
        .withMaxConnectionsPerRequestKey(Function.const(httpConfig.maxTotalConnectionsPerHost))
        .withMaxWaitQueueLimit(httpConfig.maxWaitQueueLimit)
        .withIdleTimeout(httpConfig.idleTimeout)
        .withConnectTimeout(httpConfig.connectTimeout)
        .withRequestTimeout(httpConfig.requestTimeout)
        .resource
    )

  private def recklesslyRetryPolicy[F[_]](maxWait: Duration, maxRetry: Int): RetryPolicy[F] =
    RetryPolicy(RetryPolicy.exponentialBackoff(maxWait = maxWait, maxRetry = maxRetry), (_, result) => RetryPolicy.recklesslyRetriable(result))
}
