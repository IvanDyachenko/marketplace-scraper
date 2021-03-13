package net.dalytics

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import fs2.kafka.vulcan.SchemaRegistryClientSettings

import net.dalytics.config.Config
import net.dalytics.clients.KafkaClient
import net.dalytics.models.Command
import net.dalytics.models.handler.HandlerCommand

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = init.use(_.run.executeAsync).as(ExitCode.Success)

  type AppI[+A] = Task[A]

  private def init: Resource[Task, Aggregator[AppI]] =
    for {
      implicit0(blocker: Blocker)            <- Blocker[AppI]
      cfg                                    <- Resource.liftF(Config.make[AppI])
      schemaRegistryClient                   <- Resource.liftF(SchemaRegistryClientSettings[AppI](cfg.schemaRegistryConfig.baseUrl).createSchemaRegistryClient)
      (builder, table, keySerde, valueSerde) <- KafkaClient.makeKTable[AppI, Command.Key, HandlerCommand](schemaRegistryClient)
      aggregator                             <- Aggregator.make[AppI](cfg)(builder, table, keySerde, valueSerde)
    } yield aggregator
}
