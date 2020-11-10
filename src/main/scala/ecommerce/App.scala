package ecommerce

import cats.effect.{ExitCode, IO, IOApp, Resource}
import tofu.concurrent.ContextT
import fs2.Stream
import tofu.fs2Instances._

import ecommerce.env.Environment
import ecommerce.modules.Crawler
import ecommerce.clients.EcommerceClient
import ecommerce.services.{CrawlService, EcommerceService}

object Main extends AppLogic with IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    init.use { case (env, crawler) => crawler.run.compile.drain.run(env).as(ExitCode.Success) }
}

trait AppLogic {

  type App[+A]     = ContextT[IO, Environment, A]
  type Init[+A]    = Resource[IO, A]
  type StreamF[+A] = Stream[App, A]

  def init: Init[(Environment[App], Crawler[StreamF])] =
    for {
      implicit0(ecommerceClient: EcommerceClient[App])       <- EcommerceClient.make[Init, App]
      implicit0(ecommerceService: EcommerceService[StreamF]) <- EcommerceService.make[Init, App, StreamF]
      implicit0(crawlService: CrawlService[StreamF])         <- CrawlService.make[Init, App, StreamF]
      crawler                                                <- Crawler.make[Init, App, StreamF]
    } yield Environment(ecommerceClient) -> crawler
}
