package net.dalytics

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}

import net.dalytics.config.Config

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] = init.use(_.run.executeAsync).as(ExitCode.Success)

  type AppI[+A] = Task[A]

  private def init: Resource[Task, Aggregator[AppI]] =
    for {
      implicit0(blocker: Blocker) <- Blocker[AppI]
      cfg                         <- Resource.liftF(Config.make[AppI])
      aggregator                  <- Aggregator.make[AppI](cfg)
    } yield aggregator
}
