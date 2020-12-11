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
    implicit val loggable: Loggable[Type] = Loggable.uuidLoggable.contramap(identity)
    implicit val vulcanCodec: Codec[Type] = lift
  }
  type CommandId = CommandId.Type
}
