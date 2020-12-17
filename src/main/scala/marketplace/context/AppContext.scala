package marketplace.context

import cats.{Applicative, Defer, Monad}
import cats.effect.{Blocker, ContextShift, Resource, Sync}
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.{promote, ClassyOptics}
import tofu.logging.{Loggable, LoggableContext}

import marketplace.config.Config

@ClassyOptics
final case class AppContext(
  @promote config: Config
)

object AppContext {

  def make[I[_]: Monad: Sync: ContextShift](implicit blocker: Blocker): Resource[I, AppContext] =
    Resource.liftF(Config.make[I]).map(AppContext.apply)

  implicit def loggable[F[+_]]: Loggable[AppContext] =
    Loggable.empty

  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[AppF[+*]] =
    LoggableContext.of[AppF[+*]].instance[AppContext](WithContext.apply, loggable)

  implicit def contextTSubContext[F[_]: Applicative: Defer, C](implicit
    lens: AppContext Contains C,
    wl: WithLocal[F, AppContext]
  ): F WithLocal C = WithLocal[F, AppContext].subcontext(lens)
}
