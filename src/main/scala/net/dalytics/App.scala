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
import net.dalytics.api.{OzonApi, WildBerriesApi}
import net.dalytics.modules.{Handler, Parser}
import net.dalytics.clients.{HttpClient, KafkaClient}
import net.dalytics.services.{Handle, Parse}
import net.dalytics.models.{Command, Event}
import net.dalytics.models.parser.{ParserCommand, ParserEvent}
import net.dalytics.models.handler.{HandlerCommand, HandlerEvent}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    Task
      .parZip2(
        initParser.use(_.run.compile.drain),
        initHandler.use(handler => Task.parZip2(handler.schedule.compile.drain, handler.run.compile.drain))
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

  def initHandler: Resource[Task, Handler[AppS]] =
    for {
      implicit0(blocker: Blocker)              <- Blocker[AppI]
      cfg                                      <- Resource.liftF(Config.make[AppI])
      implicit0(httpClientI: HttpClient[AppI]) <- HttpClient.make[AppI, AppI](cfg.httpConfig)
      implicit0(httpClientF: HttpClient[AppF]) <- HttpClient.make[AppI, AppF](cfg.httpConfig)
      crawl                                    <- Handle.make[AppI, AppF]
      wbApi                                    <- Resource.liftF(WildBerriesApi.make[AppI, AppS].pure[AppI])
      ozonApi                                  <- Resource.liftF(OzonApi.make[AppI, AppS].pure[AppI])
      sourcesOfCommands                         = cfg.sourcesConfig.sources.map(Handler.makeCommandsSource[AppI](_)(wbApi, ozonApi))
      producerOfEvents                         <- KafkaClient.makeProducer[AppI, Event.Key, HandlerEvent](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.handlerConfig.kafkaProducer
                                                  )
      producerOfCommands                       <- KafkaClient.makeProducer[AppI, Command.Key, HandlerCommand](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.schedulerConfig.kafkaProducer
                                                  )
      consumerOfCommands                       <- KafkaClient.makeConsumer[AppI, Command.Key, HandlerCommand](
                                                    cfg.kafkaConfig,
                                                    cfg.schemaRegistryConfig,
                                                    cfg.handlerConfig.kafkaConsumer
                                                  )
      handler                                  <- Handler.make[AppI, AppF, AppS](cfg.handlerConfig, cfg.schedulerConfig)(
                                                    crawl,
                                                    sourcesOfCommands,
                                                    producerOfEvents,
                                                    producerOfCommands,
                                                    consumerOfCommands
                                                  )
    } yield handler
}