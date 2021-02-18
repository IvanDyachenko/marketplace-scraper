package marketplace

import java.util.UUID
import java.time.Instant

import cats.Show
import tofu.logging.Loggable
import vulcan.Codec
import io.circe.{Decoder, Encoder}
import supertagged.TaggedType

package object models {

  object Timestamp extends TaggedType[Instant] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
  type Timestamp = Timestamp.Type

  trait Command {
    def id: Command.Id
    def key: Command.Key
    def created: Timestamp
  }

  object Command {
    object Id extends TaggedType[UUID] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
    type Id = Id.Type

    object Key extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
    type Key = Key.Type
  }

  trait Event {
    def id: Event.Id
    def key: Event.Key
    def created: Timestamp
  }

  object Event {
    object Id extends TaggedType[UUID] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
    type Id = Id.Type

    object Key extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
    type Key = Key.Type
  }

  trait LiftedCats {
    type Raw
    type Type

    implicit def show(implicit raw: Show[Raw]): Show[Type] = raw.asInstanceOf[Show[Type]]
  }

  trait LiftedLoggable {
    type Raw
    type Type

    implicit def loggable(implicit raw: Loggable[Raw]): Loggable[Type] = raw.asInstanceOf[Loggable[Type]]
  }

  trait LiftedCirce {
    type Raw
    type Type

    implicit def circeEncoder(implicit raw: Encoder[Raw]): Encoder[Type] = raw.asInstanceOf[Encoder[Type]]
    implicit def circeDecoder(implicit raw: Decoder[Raw]): Decoder[Type] = raw.asInstanceOf[Decoder[Type]]
  }

  trait LiftedVulcanCodec {
    type Raw
    type Type

    implicit def vulcanCodec(implicit raw: Codec[Raw]): Codec[Type] = raw.asInstanceOf[Codec[Type]]
  }
}
