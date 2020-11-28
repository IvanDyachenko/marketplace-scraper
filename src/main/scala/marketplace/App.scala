package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import cats.tagless.syntax.functorK._
import tofu.lift.{Lift}
import tofu.logging.Logs
import tofu.doobie.transactor.Txr
import tofu.doobie.instances.implicits._
import fs2.Stream
import tofu.fs2Instances._

import marketplace.db._
import marketplace.context._
import marketplace.clients._
import marketplace.modules._
import marketplace.services._
import marketplace.repositories._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    init.use { case (ctx, program) => program.run.compile.drain.run(ctx).as(ExitCode.Success) }

  type I[+A] = Task[A]
  type F[+A] = CrawlerF[A]
  type S[+A] = Stream[F, A]

  private implicit val logs: Logs[I, F] = Logs.withContext[I, F]

  def init: Resource[Task, (AppContext, Crawler[S])] =
    for {
      implicit0(be: Blocker)         <- Blocker[I]
      ctx                            <- AppContext.make[I]
      httpClient                     <- HttpClient.make[I, F](ctx.config.httpConfig)
      chXa                           <- ClickhouseXa.make[I](ctx.config.clickhouseConfig)
      chTxr                           = Txr.contextual[F](chXa)
      elh                            <- doobieLogging.makeEmbeddableLogHandler[I, F, chTxr.DB]("doobie")
      implicit0(l: Logs[I, chTxr.DB]) = logs.mapK(Lift[F, chTxr.DB].liftF)
      crawlRepo                      <- CrawlRepo.make[I, chTxr.DB](elh).map(_.mapK(chTxr.trans))
      crawlService                   <- CrawlService.make[I, F, S](httpClient, crawlRepo)
      crawler                        <- Crawler.make[I, F, S](crawlService)
    } yield (ctx, crawler)
}
