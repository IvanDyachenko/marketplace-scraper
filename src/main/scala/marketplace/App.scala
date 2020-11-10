package marketplace

import cats.effect.{ExitCode, IO, IOApp, Resource}
import tofu.concurrent.ContextT
import fs2.Stream
import tofu.fs2Instances._

import marketplace.env.Environment
import marketplace.clients.MarketplaceClient
import marketplace.modules.Crawler
import marketplace.services.CrawlService

object Main extends AppLogic with IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    init.use { case (env, program) => program.run.compile.drain.run(env).as(ExitCode.Success) }
}

trait AppLogic {
  type AppF[+A]    = ContextT[IO, Environment, A]
  type InitF[+A]   = Resource[IO, A]
  type StreamF[+A] = Stream[AppF, A]

  def init: InitF[(Environment[AppF], Crawler[StreamF])] =
    for {
      implicit0(marketplaceClient: MarketplaceClient[AppF]) <- MarketplaceClient.make[InitF, AppF]
      implicit0(crawlService: CrawlService[StreamF])        <- CrawlService.make[InitF, AppF, StreamF]
      crawler                                               <- Crawler.make[InitF, AppF, StreamF]
    } yield Environment(marketplaceClient) -> crawler
}
