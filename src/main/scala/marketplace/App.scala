package marketplace

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync, Timer}
import tofu.Execute
import tofu.lift.Unlift
import tofu.syntax.monadic._
import fs2.Stream
import tofu.fs2Instances._
import org.http4s.client.Client
//import org.http4s.client.blaze.BlazeClientBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.proxy.ProxyServer
import org.http4s.client.asynchttpclient.AsyncHttpClient

import marketplace.context._
import marketplace.modules.Crawler
import marketplace.clients.MarketplaceClient
import marketplace.services.CrawlService

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends AppLogic[IO] with IOApp {

  protected def concEff: ConcurrentEffect[IO] = implicitly

  override def run(args: List[String]): IO[ExitCode] =
    init.use { case (ctx, program) => program.run.compile.drain.run(ctx).as(ExitCode.Success) }
}

trait AppLogic[F[+_]] {

  type I[+A]       = F[A]
  type AppF[+A]    = CrawlerF[F, A]
  type StreamF[+A] = Stream[AppF, A]

  protected implicit def concEff: ConcurrentEffect[F]
  protected implicit def contextShift: ContextShift[F]
  protected implicit def timer: Timer[F]

  def init: Resource[F, (CrawlerContext[AppF], Crawler[StreamF])] =
    for {
      ctx               <- CrawlerContext.make[I, F]
      httpClient        <- buildHttp4sClient[I].map(translateHttp4sClient[I, AppF](_))
      marketplaceClient <- MarketplaceClient.make[I, AppF](httpClient)
      crawlService      <- CrawlService.make[I, AppF, StreamF](marketplaceClient)
      crawler           <- Crawler.make[I, AppF, StreamF](crawlService)
    } yield (ctx, crawler)

//  // Should be fixed in https://github.com/TinkoffCreditSystems/tofu/pull/422
//  private object Execute {
//    def apply[F[_]](implicit F: Execute[F]): F.type = F
//  }

//  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect]: Resource[F, Client[F]] =
//    Resource.liftF(Execute[F].executionContext) >>= (BlazeClientBuilder[F](_).resource)

  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect]: Resource[F, Client[F]] = {
    val proxyServer      = new ProxyServer.Builder("127.0.0.1", 8888).build() // ToDo: add HttpConfig to CrawlerConfig
    val httpClientConfig = Dsl
      .config()
      .setMaxConnections(400)
      .setMaxConnectionsPerHost(200)
      .setFollowRedirect(false)
      .setProxyServer(proxyServer)
      .build()

    AsyncHttpClient.resource(httpClientConfig)
  }

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))
}
