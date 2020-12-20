package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
//import tofu.fs2Instances._

import marketplace.context._
import marketplace.services._
import marketplace.modules._

import scala.concurrent.ExecutionContext.Implicits.global
import marketplace.clients.HttpClient

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    init.use(program => program.run.as(ExitCode.Success))

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppF, A]

  private implicit val logs: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  def init: Resource[Task, Crawler[AppI]] =
    for {
      implicit0(blocker: Blocker)             <- Blocker[AppI]
      ctx                                     <- AppContext.make[AppI]
      implicit0(httpClient: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](ctx.config.httpConfig)
      crawl                                   <- Crawl.make[AppI, AppF]
      crawler                                 <- Crawler.make[AppI, AppF](crawl)
    } yield crawler
}
