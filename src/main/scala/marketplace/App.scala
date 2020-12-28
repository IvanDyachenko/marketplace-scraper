package marketplace

import monix.eval.{Task, TaskApp}
import cats.Monad
import cats.effect.{Blocker, ExitCode, Resource, Timer}
import tofu.env.Env
import tofu.logging.Logs
import tofu.generate.GenUUID
import fs2.Stream
import tofu.fs2Instances._
import supertagged.postfix._

import marketplace.config.{Config, OzonCategorySourceConfig, SourceConfig}
import marketplace.clients.{HttpClient, KafkaClient}
import marketplace.services.{Crawl, Parse}
import marketplace.modules.{Crawler, Parser, Scheduler}
import marketplace.context.AppContext
import marketplace.models.{CommandKey, EventKey}
import marketplace.models.{crawler, ozon, parser}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = ???
    //  Task
    //    .parZip2(
    //      initCrawler.use(_.run.compile.drain),
    //      initScheduler.use(_.run.compile.drain)
    //    )
    //    .as(ExitCode.Success)
    //initParser.use(_.run.compile.drain.as(ExitCode.Success))
    //initCrawler.use(_.run.compile.drain.as(ExitCode.Success))
    //initScheduler.use(_.run.compile.drain.as(ExitCode.Success))

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logs: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  def initParser: Resource[Task, Parser[AppS]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.liftF(Config.make[AppI])
      parse                       <- Parse.make[AppI, AppF]
      consumer                    <- KafkaClient.makeConsumer[AppI, CommandKey, parser.Command](cfg.kafkaConfig, cfg.schemaRegistryConfig)(
                                       groupId = cfg.parserConfig.groupId,
                                       topic = cfg.parserConfig.commandsTopic
                                     )
      producer                    <- KafkaClient.makeProducer[AppI, EventKey, parser.Event](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      parser                      <- Parser.make[AppI, AppF, AppS](parse, consumer, producer)(cfg.parserConfig)
    } yield parser

  def initCrawler: Resource[Task, Crawler[AppS]] =
    for {
      implicit0(blocker: Blocker)             <- Blocker[AppI]
      cfg                                     <- Resource.liftF(Config.make[AppI])
      implicit0(httpClient: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      crawl                                   <- Crawl.make[AppI, AppF]
      consumer                                <- KafkaClient.makeConsumer[AppI, CommandKey, crawler.Command](cfg.kafkaConfig, cfg.schemaRegistryConfig)(
                                                   groupId = cfg.crawlerConfig.groupId,
                                                   topic = cfg.crawlerConfig.commandsTopic
                                                 )
      producer                                <- KafkaClient.makeProducer[AppI, EventKey, crawler.Event](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      crawler                                 <- Crawler.make[AppI, AppF, AppS](crawl)(cfg.crawlerConfig, consumer, producer)
    } yield crawler

  def initScheduler: Resource[Task, Scheduler[AppS]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.liftF(Config.make[AppI])
      producer                    <- KafkaClient.makeProducer[AppI, CommandKey, crawler.Command](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      sources                      = cfg.sourcesConfig.sources.map(makeSource[AppI])
      scheduler                   <- Scheduler.make[AppI, AppS, CommandKey, crawler.Command](sources)(cfg.crawlerConfig.commandsTopic, producer)
    } yield scheduler

  def makeSource[F[_]: Monad: Timer: GenUUID](config: SourceConfig): Stream[F, (CommandKey, crawler.Command)] =
    config match {
      case OzonCategorySourceConfig(name, every) =>
        Stream
          .awakeEvery[F](every)
          .zipRight {
            Stream.emits(1 to 1).evalMap { page =>
              crawler.Command.handleOzonRequest[F](
                ozon.GetCategorySearchResultsV2(
                  name,
                  page @@ ozon.Url.Page,
                  ozon.Url.LayoutContainer(ozon.Url.LayoutContainer.Name.Default),
                  page @@ ozon.Url.LayoutPageIndex
                )
              )
            }
          }
          .map(cmd => cmd.key -> cmd)
          .repeat
    }
}
