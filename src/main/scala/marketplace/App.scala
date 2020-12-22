package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._

import marketplace.config.Config
import marketplace.clients.HttpClient
import marketplace.sources.KafkaSource
import marketplace.services.{Crawl, Parse}
import marketplace.modules.{Crawler, Parser}
import marketplace.context.AppContext
import marketplace.models.{CommandKey, EventKey}
import marketplace.models.parser.{Command => ParserCommand, Event => ParserEvent}
import marketplace.models.crawler.{Command => CrawlerCommand, Event => CrawlerEvent}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    Task.parZip2(initParser.use(_.run.compile.drain), initCrawler.use(_.run.compile.drain)).as(ExitCode.Success)

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
      consumer                                <- KafkaSource.makeConsumer[AppI, CommandKey, CrawlerCommand](cfg.kafkaConfig, cfg.schemaRegistryConfig)(
                                                   groupId = cfg.crawlerConfig.groupId,
                                                   topic = cfg.crawlerConfig.commandsTopic
                                                 )
      producer                                <- KafkaSource.makeProducer[AppI, EventKey, CrawlerEvent](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      crawler                                 <- Crawler.make[AppI, AppF, AppS](crawl, consumer, producer)(cfg.crawlerConfig)
    } yield crawler

  def initParser: Resource[Task, Parser[AppS]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.liftF(Config.make[AppI])
      parse                       <- Parse.make[AppI, AppF]
      consumer                    <- KafkaSource.makeConsumer[AppI, CommandKey, ParserCommand](cfg.kafkaConfig, cfg.schemaRegistryConfig)(
                                       groupId = cfg.parserConfig.groupId,
                                       topic = cfg.parserConfig.commandsTopic
                                     )
      producer                    <- KafkaSource.makeProducer[AppI, EventKey, ParserEvent](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      parser                      <- Parser.make[AppI, AppF, AppS](parse, consumer, producer)(cfg.parserConfig)
    } yield parser
}
