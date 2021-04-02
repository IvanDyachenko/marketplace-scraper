package net.dalytics

import monix.eval.{Task, TaskApp}
import cats.effect.{ExitCode, Resource}
import tofu.env.Env
import tofu.logging.Logs
import fs2.Stream
import fs2.kafka.vulcan.SchemaRegistryClientSettings
import tofu.fs2Instances._

import net.dalytics.config.Config
import net.dalytics.context.MessageContext
import net.dalytics.models.{Command, Event}
import net.dalytics.models.parser.{ParserCommand, ParserEvent}
import net.dalytics.clients.KafkaClient
import net.dalytics.services.Parse

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = init.use(_.run.compile.drain).as(ExitCode.Success)

  type AppF[+A] = Env[MessageContext, A]
  type AppI[+A] = Task[A]
  type AppS[+A] = Stream[AppI, A]

  private implicit val logsWithContext: Logs[AppI, AppF] = Logs.withContext[AppI, AppF]

  private def init: Resource[Task, Parser[AppS]] =
    for {
      implicit0(blocker: Blocker) <- Resource.unit[AppI]
      cfg                         <- Resource.eval(Config.make[AppI])
      schemaRegistryClient        <- Resource.eval(SchemaRegistryClientSettings[AppI](cfg.schemaRegistryConfig.baseUrl).createSchemaRegistryClient)
      parse                       <- Parse.make[AppI, AppF]
      producerOfEvents            <- KafkaClient.makeProducer[AppI, Event.Key, ParserEvent](
                                       cfg.kafkaConfig,
                                       cfg.kafkaProducerConfig
                                     )(schemaRegistryClient)
      consumerOfCommands          <- KafkaClient.makeConsumer[AppI, Command.Key, ParserCommand.ParseOzonResponse](
                                       cfg.kafkaConfig,
                                       cfg.kafkaConsumerConfig
                                     )(schemaRegistryClient)
      parser                      <- Parser.make[AppI, AppF, AppS](cfg)(
                                       parse,
                                       producerOfEvents,
                                       consumerOfCommands
                                     )
    } yield parser
}
