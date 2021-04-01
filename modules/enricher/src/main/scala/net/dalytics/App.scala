package net.dalytics

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import fs2.kafka.vulcan.SchemaRegistryClientSettings

import net.dalytics.config.Config

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = init.use(_.run.executeAsync).as(ExitCode.Success)

  type AppI[+A] = Task[A]

  private def init: Resource[Task, Enricher[AppI]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.eval(Config.make[AppI])
      schemaRegistryClient        <- Resource.eval(SchemaRegistryClientSettings[AppI](cfg.schemaRegistryConfig.baseUrl).createSchemaRegistryClient)
      aggregator                  <- Enricher.make[AppI](cfg)(schemaRegistryClient)
    } yield aggregator
}
