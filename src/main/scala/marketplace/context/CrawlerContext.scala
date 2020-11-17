package marketplace.context

import cats.{Applicative, Defer, Monad}
import cats.effect.{Resource, Sync}
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.{promote, ClassyOptics}
import tofu.logging.{Loggable, LoggableContext, Logs}

import marketplace.config.CrawlerConfig

@ClassyOptics
final case class CrawlerContext(
  @promote config: CrawlerConfig
)

object CrawlerContext {

  def make[I[_]: Monad]: Resource[I, CrawlerContext] =
    Resource.liftF(CrawlerConfig.make[I]).map(CrawlerContext.apply)

  implicit def loggable[F[+_]]: Loggable[CrawlerContext] =
    Loggable.empty

  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[CrawlerF[+*]] =
    LoggableContext.of[CrawlerF[+*]].instance[CrawlerContext](WithContext.apply, loggable)

  implicit def logs[F[+_]: Sync]: Logs[F, CrawlerF[*]] =
    Logs.withContext[F, CrawlerF[*]]

  implicit def contextTSubContext[F[_]: Applicative: Defer, C](implicit
    lens: CrawlerContext Contains C,
    wl: WithLocal[F, CrawlerContext]
  ): F WithLocal C = WithLocal[F, CrawlerContext].subcontext(lens)
}
