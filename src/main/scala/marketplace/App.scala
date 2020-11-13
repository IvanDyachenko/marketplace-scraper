package marketplace

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import tofu.Execute
import tofu.lift.Unlift
import tofu.syntax.monadic._
import fs2.Stream
import tofu.fs2Instances._

import marketplace.context._
import marketplace.modules.Crawler
import marketplace.clients.MarketplaceClient
import marketplace.services.CrawlService
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends AppLogic[IO] with IOApp {

  protected def concEff: ConcurrentEffect[IO] = implicitly

  override def run(args: List[String]): IO[ExitCode] =
    init.use { case (ctx, program) => program.run.compile.drain.run(ctx).as(ExitCode.Success) }
}

trait AppLogic[F[+_]] {

  type I[+A]       = F[A]
  type AppF[+A]    = CrawlerF[F, A]
  type InitF[+A]   = Resource[F, A]
  type StreamF[+A] = Stream[AppF, A]

  protected implicit def concEff: ConcurrentEffect[F]
  protected implicit def contextShift: ContextShift[F]

  def init: InitF[(CrawlerContext[AppF], Crawler[StreamF])] =
    for {
      ctx                                                   <- CrawlerContext.make[I, F]
      httpClientI                                           <- buildHttp4sClient[I]
      implicit0(httpClient: Client[AppF])                    = translateHttp4sClient[I, AppF](httpClientI)
      implicit0(marketplaceClient: MarketplaceClient[AppF]) <- MarketplaceClient.make[I, AppF]
      implicit0(crawlService: CrawlService[StreamF])        <- CrawlService.make[InitF, AppF, StreamF]
      crawler                                               <- Crawler.make[InitF, AppF, StreamF]
    } yield (ctx, crawler)

  private object Execute {
    def apply[F[_]](implicit F: Execute[F]): F.type = F
  }

  private def buildHttp4sClient[F[_]: Execute: ConcurrentEffect]: Resource[F, Client[F]] =
    Resource.liftF(Execute[F].executionContext) >>= (BlazeClientBuilder[F](_).resource)

  // https://scastie.scala-lang.org/Odomontois/F29lLrY2RReZrcUJ1zIEEg/25
  private def translateHttp4sClient[F[_]: Sync, G[_]: Sync](client: Client[F])(implicit U: Unlift[F, G]): Client[G] =
    Client(req => Resource.suspend(U.unlift.map(gf => client.run(req.mapK(gf)).mapK(U.liftF).map(_.mapK(U.liftF)))))
}
