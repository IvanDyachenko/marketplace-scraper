package marketplace.context

import cats.{Defer, Monad}
import cats.effect.{Resource, Sync}
import tofu.syntax.monadic._
import tofu.lift.Lift
import tofu.optics.macros.ClassyOptics
import tofu.logging.{Logging, Logs}

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
