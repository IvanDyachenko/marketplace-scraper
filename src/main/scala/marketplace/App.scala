package marketplace

import tofu.env.Env
import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._

import marketplace.context._
import marketplace.clients._
import marketplace.modules._
import marketplace.services._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    init.use { case (ctx, program) => program.run.compile.drain.run(ctx).as(ExitCode.Success) }

  type I[+A] = Task[A]
  type F[+A] = Env[AppContext, A]
  type S[+A] = Stream[F, A]

  private implicit val logs: Logs[I, F] = Logs.withContext[I, F]

  def init: Resource[Task, (AppContext, Crawler[S])] =
    for {
      implicit0(blocker: Blocker) <- Blocker[I]
//    wr                           = implicitly[WithRun[F, I, AppContext]]
      env                         <- AppContext.make[I]
      httpClient                  <- HttpClient.make[I, F](env.config.httpConfig)
      crawl                       <- Crawl.make[I, F, S](httpClient)
      crawler                     <- Crawler.make[I, F, S](env.config.schemaRegistryConfig, crawl)
    } yield (env, crawler)
}
