package marketplace.context

import cats.{Applicative, Defer}
import cats.effect.{ConcurrentEffect, Resource, Sync}
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.ClassyOptics
import tofu.logging.{Loggable, LoggableContext, Logs}

@ClassyOptics
final case class CrawlerContext[F[_]](
  loggers: Loggers[F]
)

object CrawlerContext {

  def make[F[+_]: ConcurrentEffect]: Resource[F, CrawlerContext[CrawlerF[F, *]]] =
    for {
      loggers <- Resource.liftF(Loggers.make[F])
    } yield CrawlerContext(loggers)

  implicit def loggable[F[+_]]: Loggable[CrawlerContext[F]] =
    Loggable.empty

  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[CrawlerF[F, +*]] =
    LoggableContext.of[CrawlerF[F, +*]].instance[CrawlerContext[CrawlerF[F, +*]]](WithContext.apply, loggable)

  implicit def logs[F[+_]: Sync]: Logs[F, CrawlerF[F, *]] =
    Logs.withContext[F, CrawlerF[F, *]]

  implicit def subContext[F[_], C](implicit
    lens: CrawlerContext[F] Contains C,
    wl: WithLocal[F, CrawlerContext[F]]
  ): F WithLocal C =
    WithLocal[F, CrawlerContext[F]].subcontext(lens)
}
