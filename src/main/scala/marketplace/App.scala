package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
//import tofu.fs2Instances._

import marketplace.context.AppContext
import marketplace.modules.Crawler
import marketplace.services.Crawl
import marketplace.clients.{HttpClient, KafkaClient}
import marketplace.models.{CommandId, EventId}
import marketplace.models.crawler.{Command, Event}

import scala.concurrent.ExecutionContext.Implicits.global

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
      cfg                                      = ctx.config
      implicit0(httpClient: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      crawl                                   <- Crawl.make[AppI, AppF]
      consumer                                <- KafkaClient.makeConsumer[AppI, CommandId, Command](cfg.kafkaConfig, cfg.schemaRegistryConfig)(cfg.crawlerConfig.groupId)
      producer                                <- KafkaClient.makeProducer[AppI, EventId, Event](cfg.kafkaConfig, cfg.schemaRegistryConfig)
      crawler                                 <- Crawler.make[AppI, AppF](crawl, consumer, producer)(cfg.crawlerConfig)
    } yield crawler
}
