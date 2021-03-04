package net.dalytics

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._
import tofu.syntax.monadic._

import net.dalytics.config.Config
import net.dalytics.context.AppContext
import net.dalytics.models.Command
import net.dalytics.models.handler.HandlerCommand
import net.dalytics.clients.{HttpClient, KafkaClient}
import net.dalytics.api.{OzonApi, WildBerriesApi}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = init.use(_.run.compile.drain).as(ExitCode.Success)

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logsSync: Logs[AppI, AppI] = Logs.sync[AppI, AppI]

  private def init: Resource[Task, Scheduler[AppS]] =
    for {
      implicit0(blocker: Blocker)              <- Blocker[AppI]
      cfg                                      <- Resource.liftF(Config.make[AppI])
      implicit0(httpClientI: HttpClient[AppI]) <- HttpClient.make[AppI, AppI](cfg.httpConfig)
      wbApi                                    <- Resource.liftF(WildBerriesApi.make[AppI, AppS].pure[AppI])
      ozonApi                                  <- Resource.liftF(OzonApi.make[AppI, AppS].pure[AppI])
      sourcesOfCommands                         = cfg.sourcesConfig.sources.map(Scheduler.makeCommandsSource[AppI](_)(wbApi, ozonApi))
      producerOfCommands                       <- KafkaClient.makeProducer[AppI, Command.Key, HandlerCommand](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.kafkaProducerConfig
                                                  )
      scheduler                                <- Scheduler.make[AppI, AppF, AppS](cfg)(
                                                    sourcesOfCommands,
                                                    producerOfCommands
                                                  )
    } yield scheduler
}
