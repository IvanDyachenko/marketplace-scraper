package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._

import marketplace.config.Config
import marketplace.context.AppContext
import marketplace.models.{Command, Event}
import marketplace.models.crawler.{CrawlerCommand, CrawlerEvent}
import marketplace.clients.{HttpClient, KafkaClient}
import marketplace.services.Crawl
import marketplace.modules.Crawler

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = ???

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
}
