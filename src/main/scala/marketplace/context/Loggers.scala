package marketplace.context

import cats.effect.Sync
import tofu.syntax.monadic._
import tofu.logging.{Logging, Logs}
import tofu.optics.macros.ClassyOptics

import marketplace.context.CrawlerContext.CrawlerF

@ClassyOptics
case class Loggers[F[_]](
  requests: Logging[F]
)

object Loggers {

  def make[F[+_]: Sync](implicit logs: Logs[F, CrawlerF[F, *]]): F[Loggers[CrawlerF[F, *]]] =
    logs.byName("requests").map(Loggers.apply)
}

//class CrawlerLog[label]
//
//object CrawlerLog {
//
//  type Of[F[_], label] = ServiceLogging[F, CrawlerLog[label]]
//
//  implicit def extractLogging[F[_]: FlatMap, label](implicit
//    lens: (Loggers[F] Contains Logging[F]) with Label[label],
//    wc: WithContext[F, Loggers[F]]
//  ): Of[F, label] = Embed.of(wc.ask(lens.get)).to
//}
