package marketplace.context

import cats.{Applicative, Defer, Monad}
import cats.effect.{ConcurrentEffect, Resource, Sync}
import tofu.{WithContext, WithLocal}
import tofu.lift.Lift
import tofu.logging.{Loggable, LoggableContext, Logs}
import tofu.optics.Contains
import tofu.optics.macros.{promote, ClassyOptics}

import marketplace.config.CrawlerConfig

@ClassyOptics
final case class CrawlerContext[F[_]](
  @promote config: CrawlerConfig
)

object CrawlerContext {

  def make[I[_]: Defer: Monad: Lift[F, *[_]], F[+_]: ConcurrentEffect]: Resource[I, CrawlerContext[CrawlerF[F, *]]] =
    for {
      config <- Resource.liftF(CrawlerConfig.make[I])
    } yield CrawlerContext(config)

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
