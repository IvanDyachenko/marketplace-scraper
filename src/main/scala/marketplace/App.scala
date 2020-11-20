package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
//import tofu.syntax.monadic._
import cats.tagless.syntax.functorK._
import tofu.lift.{Lift}
import tofu.doobie.transactor.Txr
import tofu.doobie.instances.implicits._
import fs2.Stream
import tofu.fs2Instances._
import tofu.logging.Logs

import marketplace.db._
//import marketplace.config._
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

  def init: Resource[Task, (CrawlerContext, Crawler[S])] =
    for {
      implicit0(blocker: Blocker)  <- Blocker[I]
      ctx                          <- CrawlerContext.make[I]
      xa                           <- ClickhouseXa.make[I](ctx.config.clickhouseConfig)
      txr                           = Txr.contextual[F](xa)
      elh                          <- doobieLogging.makeEmbeddableLogHandler[I, F, txr.DB]("doobie")
      httpClient                   <- HttpClient.make[I, F](ctx.config.httpConfig)
      crawlService                 <- CrawlService.make[I, F, S](httpClient)
      implicit0(l: Logs[I, txr.DB]) = logs.mapK(Lift[F, txr.DB].liftF)
      marketplaceRepo              <- MarketplaceRepo.make[I, txr.DB](elh)
      marketplaceRepoF              = marketplaceRepo.mapK(txr.trans)
      marketplaceService           <- MarketplaceService.make[I, F](marketplaceRepoF)
      crawler                      <- Crawler.make[I, F, S](crawlService, marketplaceService)
    } yield (ctx, crawler)
}
