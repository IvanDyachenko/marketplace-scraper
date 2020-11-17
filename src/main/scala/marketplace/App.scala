package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{ConcurrentEffect, ExitCode, Resource, Sync}
import tofu.syntax.monadic._
import tofu.Execute
import tofu.lift.Unlift
import tofu.doobie.transactor.Txr
import tofu.doobie.instances.implicits._
import fs2.Stream
import tofu.fs2Instances._
import org.http4s.client.Client
//import org.http4s.client.blaze.BlazeClientBuilder
import org.asynchttpclient.Dsl
import org.asynchttpclient.proxy.ProxyServer
import org.http4s.client.asynchttpclient.AsyncHttpClient

import marketplace.db._
import marketplace.config._
import marketplace.context._
import marketplace.clients._
import marketplace.modules._
import marketplace.services._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    init.use { case (ctx, program) => program.run.compile.drain.run(ctx).as(ExitCode.Success) }

  type I[+A] = Task[A]
  type F[+A] = CrawlerF[A]
  type S[+A] = Stream[F, A]

  def init: Resource[Task, (CrawlerContext, Crawler[S])] =
    for {
      ctx               <- CrawlerContext.make[I]
      xa                <- ClickhouseXa.make[I](ctx.config.clickhouseConfig)
      txr                = Txr.contextual[F](xa)
      elh               <- doobieLogging.makeEmbeddableLogHandler[I, F, txr.DB]("doobie")
      httpClient        <- buildHttp4sClient[I](ctx.config.httpConfig).map(translateHttp4sClient[I, F](_))
      marketplaceClient <- MarketplaceClient.make[I, F](httpClient)
      crawlService      <- CrawlService.make[I, F, S](marketplaceClient)
      crawler           <- Crawler.make[I, F, S](crawlService)
    } yield (ctx, crawler)

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))

  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect](httpConfig: HttpConfig): Resource[F, Client[F]] = {
    val HttpConfig(proxyHost, proxyPort, maxConnections, maxConnectionsPerHost) = httpConfig

    val proxyServer = new ProxyServer.Builder(proxyHost, proxyPort).build()

    val httpClientConfig = Dsl
      .config()
      .setMaxConnections(maxConnections)
      .setMaxConnectionsPerHost(maxConnectionsPerHost)
      .setFollowRedirect(false)
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
