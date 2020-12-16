package marketplace

import cats.Show
import supertagged.TaggedType
import vulcan.Codec
import tofu.logging.Loggable
import derevo.derive
import tofu.logging.derivation.loggable

import java.util.UUID
import java.time.Instant

package object models {

  @derive(loggable)
  case class Timestamp(value: Instant)

  object Timestamp {
    implicit val vulcanCodec: Codec[Timestamp] = Codec.instant.imap(apply)(_.value)
  }

  object CommandId extends TaggedType[UUID] {
    implicit val show: Show[Type]         = Show.fromToString
    implicit val loggable: Loggable[Type] = lift
    implicit val vulcanCodec: Codec[Type] = lift
  }
  type CommandId = CommandId.Type

  object CommandKey extends TaggedType[String] {
    implicit val show: Show[Type]         = Show.fromToString
    implicit val loggable: Loggable[Type] = lift
    implicit val vulcanCodec: Codec[Type] = lift
  }
  type CommandKey = CommandKey.Type

  object EventId extends TaggedType[UUID] {
    implicit val show: Show[Type]         = Show.fromToString
    implicit val loggable: Loggable[Type] = lift
    implicit val vulcanCodec: Codec[Type] = lift
  }
  type EventId = EventId.Type

  object EventKey extends TaggedType[String] {
    implicit val show: Show[Type]         = Show.fromToString
    implicit val loggable: Loggable[Type] = lift
    implicit val vulcanCodec: Codec[Type] = lift
  }
  type EventKey = EventKey.Type
}
