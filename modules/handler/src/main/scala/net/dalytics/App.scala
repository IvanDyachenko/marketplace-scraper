package net.dalytics

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._

import net.dalytics.config.Config
import net.dalytics.context.AppContext
import net.dalytics.models.{Command, Event}
import net.dalytics.models.handler.{HandlerCommand, HandlerEvent}
import net.dalytics.clients.{HttpClient, KafkaClient}
import net.dalytics.services.Handle

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = init.use(_.run.compile.drain).as(ExitCode.Success)

  type AppF[+A] = Env[AppContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logsWithContext: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  private def init: Resource[Task, Handler[AppS]] =
    for {
      implicit0(blocker: Blocker)              <- Blocker[AppI]
      cfg                                      <- Resource.liftF(Config.make[AppI])
      implicit0(httpClientF: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      handle                                   <- Handle.make[AppI, AppF]
      producerOfEvents                         <- KafkaClient.makeProducer[AppI, Event.Key, HandlerEvent](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.kafkaProducerConfig
                                                  )
      consumerOfCommands                       <- KafkaClient.makeConsumer[AppI, Command.Key, HandlerCommand](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.kafkaConsumerConfig
                                                  )
      handler                                  <- Handler.make[AppI, AppF, AppS](cfg)(
                                                    handle,
                                                    producerOfEvents,
                                                    consumerOfCommands
                                                  )
    } yield handler
}
