package marketplace.context

import cats.{Applicative, Defer}
import cats.effect.Sync
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.ClassyOptics
import tofu.concurrent.ContextT
import tofu.logging.{Loggable, LoggableContext, Logs}
import cats.effect.Resource
import cats.effect.ConcurrentEffect

@ClassyOptics
final case class CrawlerContext[F[_]](
  loggers: Loggers[F]
)

object CrawlerContext {

  type CrawlerF[F[+_], +A] = ContextT[F, CrawlerContext, A]

  def make[F[+_]: ConcurrentEffect]: Resource[F, CrawlerContext[CrawlerF[F, *]]] = ???

  implicit def loggable[F[+_]]: Loggable[CrawlerContext[F]] =
    Loggable.empty

  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[CrawlerF[F, +*]] =
    LoggableContext.of[CrawlerF[F, +*]].instance[CrawlerContext[CrawlerF[F, +*]]](WithContext.apply, loggable)

  implicit def logs[F[+_]: Sync]: Logs[F, CrawlerF[F, *]] =
    Logs.withContext[F, CrawlerF[F, *]]

  // format: off
  implicit def subContext[F[_], C](implicit lens: CrawlerContext[F] Contains C, wl: WithLocal[F, CrawlerContext[F]]): F WithLocal C =
    WithLocal[F, CrawlerContext[F]].subcontext(lens)
  // format: on
}
