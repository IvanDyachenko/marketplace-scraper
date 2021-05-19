package net.dalytics.context

import cats.{Applicative, Defer}
import tofu.{WithContext, WithLocal}
import tofu.optics.Contains
import tofu.optics.macros.ClassyOptics
import tofu.logging.{Loggable, LoggableContext}
import derevo.derive
import tofu.logging.derivation.loggable

@ClassyOptics
@derive(loggable)
final case class MessageContext(
  consumerGroupId: Option[String],
  topic: String,
  partition: Int,
  offset: Long,
  metadata: String
)

object MessageContext {

  implicit def loggableContext[F[+_]: Applicative: Defer]: LoggableContext[AppF[+*]] =
    LoggableContext.of[AppF[+*]].instance[MessageContext](WithContext.apply, Loggable[MessageContext])

  implicit def subContext[F[_]: Applicative: Defer, C](implicit lens: MessageContext Contains C, wl: WithLocal[F, MessageContext]): F WithLocal C =
    WithLocal[F, MessageContext].subcontext(lens)
}
