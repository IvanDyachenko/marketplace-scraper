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
import marketplace.services.Crawl
import marketplace.modules.Crawler
import marketplace.context.AppContext
import marketplace.models.{CommandKey, EventKey}
import marketplace.models.crawler.{Command, Event}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    init.use(program => program.run.compile.drain.as(ExitCode.Success))

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logs: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  def init: Resource[Task, Crawler[AppS]] =
    for {
      implicit0(blocker: Blocker)             <- Blocker[AppI]
      cfg                                     <- Resource.liftF(Config.make[AppI])
      implicit0(httpClient: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      crawl                                   <- Crawl.make[AppI, AppF]
      consumer                                <- KafkaSource.makeConsumer[AppI, CommandKey, Command](cfg.kafkaConfig, cfg.schemaRegistryConfig)(
                                                   groupId = cfg.crawlerConfig.groupId,
                                                   topic = cfg.crawlerConfig.commandsTopic
                                                 )
      producer                                <- KafkaSource.makeProducer[AppI, EventKey, Event](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      crawler                                 <- Crawler.make[AppI, AppF, AppS](crawl, consumer, producer)(cfg.crawlerConfig)
    } yield crawler
}
