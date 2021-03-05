package net.dalytics.context

import cats.{Applicative, Defer}
import cats.effect.{Blocker, ContextShift, Resource, Sync}
import tofu.syntax.monadic._
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.ClassyOptics
import tofu.logging.{Loggable, LoggableContext}

@ClassyOptics
final case class CommandContext()

object CommandContext {

  implicit def loggable[F[+_]]: Loggable[CommandContext] = Loggable.empty

  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[AppF[+*]] =
    LoggableContext.of[AppF[+*]].instance[CommandContext](WithContext.apply, loggable)

  implicit def subContext[F[_]: Applicative: Defer, C](implicit lens: CommandContext Contains C, wl: WithLocal[F, CommandContext]): F WithLocal C =
    WithLocal[F, CommandContext].subcontext(lens)

  def make[I[_]: Sync: ContextShift](implicit blocker: Blocker): Resource[I, CommandContext] = Resource.liftF(apply().pure[I])
}
