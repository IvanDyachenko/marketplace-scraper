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

  type I[+A] = Task[A]
  type S[+A] = Stream[App, A]

  private implicit val logs: Logs[I, App] = Logs.withContext[I, App]

  def init: Resource[Task, (Environment, Crawler[S])] =
    for {
      implicit0(blocker: Blocker)       <- Blocker[I]
      env                               <- Environment.make[I]
      wr                                 = implicitly[WithRun[App, I, Environment]]
      implicit0(avro: AvroSettings[App]) = env.avroSettings
      crawl                             <- Crawl.make[I, App]
      crawler                           <- Crawler.make[I, App, S](env.config.crawlerConfig, env.config.kafkaConfig, crawl)
    } yield (env, crawler)
}
