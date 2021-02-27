package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._
import tofu.syntax.monadic._

import marketplace.config.Config
import marketplace.context.AppContext
import marketplace.api.{OzonApi, WildBerriesApi}
import marketplace.modules.{Crawler, Parser}
import marketplace.clients.{HttpClient, KafkaClient}
import marketplace.services.{Crawl, Parse}
import marketplace.models.{Command, Event}
import marketplace.models.parser.{ParserCommand, ParserEvent}
import marketplace.models.crawler.{CrawlerCommand, CrawlerEvent}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    initCrawler
      .use(crawler =>
        initParser.use(parser =>
          Task.parZip3(
            parser.run.compile.drain.executeAsync,
            crawler.run.compile.drain.executeAsync,
            crawler.schedule.compile.drain.executeAsync
          )
        )
      )
      .as(ExitCode.Success)

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logsSync: Logs[AppI, AppI]        = Logs.sync[AppI, AppI]
  private implicit val logsWithContext: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  def initParser: Resource[Task, Parser[AppS]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.liftF(Config.make[AppI])
      parse                       <- Parse.make[AppI, AppF]
      producerOfEvents            <- KafkaClient.makeProducer[AppI, Event.Key, ParserEvent](
                                       cfg.kafkaConfig,
                                       cfg.schemaRegistryConfig,
                                       cfg.parserConfig.kafkaProducer
                                     )
      consumerOfCommands          <- KafkaClient.makeConsumer[AppI, Command.Key, ParserCommand.ParseOzonResponse](
                                       cfg.kafkaConfig,
                                       cfg.schemaRegistryConfig,
                                       cfg.parserConfig.kafkaConsumer
                                     )
      parser                      <- Parser.make[AppI, AppF, AppS](cfg.parserConfig)(
                                       parse,
                                       producerOfEvents,
                                       consumerOfCommands
                                     )
    } yield parser

  def initCrawler: Resource[Task, Crawler[AppS]] =
    for {
      implicit0(blocker: Blocker)              <- Blocker[AppI]
      cfg                                      <- Resource.liftF(Config.make[AppI])
      implicit0(httpClientI: HttpClient[AppI]) <- HttpClient.make[AppI, AppI](cfg.httpConfig)
      implicit0(httpClientF: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      crawl                                    <- Crawl.make[AppI, AppF]
      wbApi                                    <- Resource.liftF(WildBerriesApi.make[AppI, AppS].pure[AppI])
      ozonApi                                  <- Resource.liftF(OzonApi.make[AppI, AppS].pure[AppI])
      sourcesOfCommands                         = cfg.sourcesConfig.sources.map(Crawler.makeCommandsSource[AppI](_)(wbApi, ozonApi))
      producerOfEvents                         <- KafkaClient.makeProducer[AppI, Event.Key, CrawlerEvent](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.crawlerConfig.kafkaProducer
                                                  )
      producerOfCommands                       <- KafkaClient.makeProducer[AppI, Command.Key, CrawlerCommand](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.schedulerConfig.kafkaProducer
                                                  )
      consumerOfCommands                       <- KafkaClient.makeConsumer[AppI, Command.Key, CrawlerCommand](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.crawlerConfig.kafkaConsumer
                                                  )
      crawler                                  <- Crawler.make[AppI, AppF, AppS](cfg.crawlerConfig, cfg.schedulerConfig)(
                                                    crawl,
                                                    sourcesOfCommands,
                                                    producerOfEvents,
                                                    producerOfCommands,
                                                    consumerOfCommands
                                                  )
    } yield crawler
}
