package marketplace

import monix.eval.{Task, TaskApp}
import cats.Monad
import cats.effect.{Blocker, ExitCode, Resource, Timer}
import tofu.env.Env
import tofu.logging.Logs
import tofu.generate.GenUUID
import fs2.Stream
import tofu.fs2Instances._

import marketplace.config.{Config, SourceConfig}
import marketplace.context.AppContext
import marketplace.models.{ozon, Command, Event}
import marketplace.models.crawler.{CrawlerCommand, CrawlerEvent}
import marketplace.clients.{HttpClient, KafkaClient}
import marketplace.services.{Crawl, Source}
import marketplace.modules.{Crawler, Publisher}
import supertagged.postfix._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    Task
      .parZip2(
        initCrawler.use(_.run.compile.drain),
        initPublisher.use(_.run.compile.drain)
      )
      .as(ExitCode.Success)

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logs: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  def initCrawler: Resource[Task, Crawler[AppS]] =
    for {
      implicit0(blocker: Blocker)             <- Blocker[AppI]
      cfg                                     <- Resource.liftF(Config.make[AppI])
      implicit0(httpClient: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      crawl                                   <- Crawl.make[AppI, AppF]
      consumer                                <- KafkaClient.makeConsumer[AppI, Command.Key, CrawlerCommand](cfg.kafkaConfig, cfg.schemaRegistryConfig)(
                                                   groupId = cfg.crawlerConfig.groupId,
                                                   topic = cfg.crawlerConfig.commandsTopic
                                                 )
      producer                                <- KafkaClient.makeProducer[AppI, Event.Key, CrawlerEvent](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      crawler                                 <- Crawler.make[AppI, AppF, AppS](cfg.crawlerConfig)(crawl, consumer, producer)
    } yield crawler

  def initPublisher: Resource[Task, Publisher[AppS]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.liftF(Config.make[AppI])
      producer                    <- KafkaClient.makeProducer[AppI, Command.Key, CrawlerCommand](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      commands                     = cfg.sourcesConfig.sources.map(makeCrawlerCommandSource[AppI])
      publisher                   <- Publisher.make[AppI, AppS, Command.Key, CrawlerCommand](commands)(cfg.crawlerConfig.commandsTopic, producer)
    } yield publisher

  def makeCrawlerCommandSource[F[_]: Monad: Timer: GenUUID](config: SourceConfig): Source[Stream[F, *], (Command.Key, CrawlerCommand)] =
    config match {
      case SourceConfig.OzonCategory(name, SourceConfig.Pages.Top, _) =>
        Source.make(config) {
          Stream
            .eval(CrawlerCommand.handleOzonRequest[F](ozon.GetCategorySearchResultsV2(name, 1 @@ ozon.Url.Page)))
            .map(cmd => cmd.key -> cmd)
        }
      case SourceConfig.OzonCategory(name, SourceConfig.Pages.All, _) =>
        Source.make(config) {
          Stream
            .emits(1 to 20) // FixMe
            .evalMap { page =>
              CrawlerCommand.handleOzonRequest[F](ozon.GetCategorySearchResultsV2(name, page @@ ozon.Url.Page))
            }
            .map(cmd => cmd.key -> cmd)
        }
    }
}
