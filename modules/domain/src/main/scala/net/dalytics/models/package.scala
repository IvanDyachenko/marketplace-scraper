package net.dalytics

import java.time.Instant
import java.nio.charset.StandardCharsets.UTF_8

import cats.Show
import cats.effect.Sync
import tofu.logging.Loggable
import vulcan.{AvroError, Codec}
import tethys.JsonReader
import io.circe.{Decoder, Encoder, Json}
import io.circe.parser.decode
import org.http4s.EntityDecoder
import supertagged.TaggedType

package object models {

  object Timestamp extends TaggedType[Instant] with LiftedOrdered with LiftedCats with LiftedLoggable with LiftedVulcanCodec
  type Timestamp = Timestamp.Type

  trait Command {
    def key: Option[Command.Key] = None
    def created: Timestamp
  }

  object Command {
    object Key extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
    type Key = Key.Type
  }

  trait Event {
    def key: Option[Event.Key] = None
    def created: Timestamp
  }

  object Event {
    object Key extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
    type Key = Key.Type
  }

  object Raw extends TaggedType[String] {
    implicit val loggable: Loggable[Type] = Loggable.empty
    implicit val vulcanCodec: Codec[Type] = Codec.bytes.imap(bytes => new String(bytes, UTF_8))(_.getBytes(UTF_8)).asInstanceOf[Codec[Type]]

    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Type] =
      EntityDecoder.byteArrayDecoder[F].map(bytes => new String(bytes, UTF_8)).asInstanceOf[EntityDecoder[F, Type]]
  }
  type Raw = Raw.Type

  trait LiftedOrdered {
    type Raw
    type Type

    implicit def ordered(implicit raw: Ordered[Raw]): Ordered[Type] = raw.asInstanceOf[Ordered[Type]]
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

  trait LiftedTethys {
    type Raw
    type Type

    implicit def jsonReader(implicit raw: JsonReader[Raw]): JsonReader[Type] = raw.asInstanceOf[JsonReader[Type]]
  }

  trait LiftedVulcanCodec {
    type Raw
    type Type

    implicit def vulcanCodec(implicit raw: Codec[Raw]): Codec[Type] = raw.asInstanceOf[Codec[Type]]
  }

  implicit val jsonLoggable: Loggable[Json] = Loggable.empty
  implicit val jsonVulcanCodec: Codec[Json] = Codec.bytes.imapError { bytes =>
    decode[Json](new String(bytes, UTF_8)).left.map(err => AvroError(err.getMessage))
  }(_.noSpaces.getBytes(UTF_8))
}
