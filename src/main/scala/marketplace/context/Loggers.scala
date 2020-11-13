package marketplace.context

import cats.{Defer, Monad}
import cats.effect.{Resource, Sync}
import tofu.syntax.monadic._
import tofu.lift.Lift
import tofu.logging.{Logging, Logs}
import tofu.optics.macros.ClassyOptics

@ClassyOptics
case class Loggers[F[_]](
  trace: Logging[F],
  requests: Logging[F]
)

object Loggers {

  def make[F[+_]: Sync](implicit logs: Logs[F, CrawlerF[F, *]]): F[Loggers[CrawlerF[F, *]]] =
    (logs.byName("trace"), logs.byName("requests")).mapN(Loggers.apply)

  def make[I[_]: Monad: Defer, F[+_]: Sync](implicit
    L: Lift[F, I],
    logs: Logs[F, CrawlerF[F, *]]
  ): Resource[I, Loggers[CrawlerF[F, *]]] =
    Resource.liftF(Loggers.make[F]).mapK(L.liftF)
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
