package marketplace

import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Resource}
import fs2.Stream
import tofu.fs2Instances._

import marketplace.context._
import marketplace.modules.Crawler
import marketplace.clients.MarketplaceClient
import marketplace.services.CrawlService

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

  def init: InitF[(CrawlerContext[AppF], Crawler[StreamF])] =
    for {
      ctx                                                   <- CrawlerContext.make[I, F]
      implicit0(marketplaceClient: MarketplaceClient[AppF]) <- MarketplaceClient.make[InitF, AppF]
      implicit0(crawlService: CrawlService[StreamF])        <- CrawlService.make[InitF, AppF, StreamF]
      crawler                                               <- Crawler.make[InitF, AppF, StreamF]
    } yield (ctx, crawler)
}
