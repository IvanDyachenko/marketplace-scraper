package ecommerce

import cats.effect.{ExitCode, IO, IOApp, Resource}
import tofu.concurrent.ContextT
import fs2.Stream
import tofu.fs2Instances._

import ecommerce.env.Environment
import ecommerce.modules.Crawler
import ecommerce.clients.EcommerceClient
import ecommerce.services.CrawlService

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
      implicit0(ecommerceClient: EcommerceClient[AppF]) <- EcommerceClient.make[InitF, AppF]
      implicit0(crawlService: CrawlService[StreamF])    <- CrawlService.make[InitF, AppF, StreamF]
      crawler                                           <- Crawler.make[InitF, AppF, StreamF]
    } yield Environment(ecommerceClient) -> crawler
}
