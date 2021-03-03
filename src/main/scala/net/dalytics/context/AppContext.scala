package net.dalytics.context

import cats.{Applicative, Defer}
import cats.effect.{Blocker, ContextShift, Resource, Sync}
import tofu.syntax.monadic._
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.ClassyOptics
import tofu.logging.{Loggable, LoggableContext}

@ClassyOptics
final case class AppContext()

object AppContext {

  implicit def loggable[F[+_]]: Loggable[AppContext]                                 =
    Loggable.empty
  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[AppF[+*]] =
    LoggableContext.of[AppF[+*]].instance[AppContext](WithContext.apply, loggable)

  implicit def subContext[F[_]: Applicative: Defer, C](implicit
    lens: AppContext Contains C,
    wl: WithLocal[F, AppContext]
  ): F WithLocal C = WithLocal[F, AppContext].subcontext(lens)

  def make[I[_]: Sync: ContextShift](implicit blocker: Blocker): Resource[I, AppContext] =
    Resource.liftF(apply().pure[I])
}
