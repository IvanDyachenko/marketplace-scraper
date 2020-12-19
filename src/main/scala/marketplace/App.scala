package marketplace

import monix.eval.{Task, TaskApp}
import cats.effect.{Blocker, ExitCode, Resource}
import tofu.WithRun
import tofu.logging.Logs
import fs2.Stream
import tofu.fs2Instances._
import fs2.kafka.vulcan.AvroSettings

import marketplace.env._
import marketplace.services._
import marketplace.modules._

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends TaskApp {

  override def run(args: List[String]): Task[ExitCode] =
    init.use { case (env, program) => program.run.compile.drain.run(env).as(ExitCode.Success) }

  type Init[+A] = Task[A]
  type AppS[+A] = Stream[App, A]

  private implicit val logsApp: Logs[Init, App] = Logs.withContext[Init, App]
//private implicit val logsInit: Logs[Init, Init] = Logs.sync[Init, Init]

  def init: Resource[Task, (Environment, Crawler[AppS])] =
    for {
      implicit0(blocker: Blocker)       <- Blocker[Init]
      env                               <- Environment.make[Init]
      wr                                 = implicitly[WithRun[App, Init, Environment]]
      implicit0(avro: AvroSettings[App]) = env.avroSettings
      crawl                             <- Crawl.make[Init, App]
      crawler                           <- Crawler.make[Init, App, AppS](env.config.crawlerConfig, env.config.kafkaConfig, crawl)
    } yield (env, crawler)
}
