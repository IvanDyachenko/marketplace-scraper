package net.dalytics

import java.time.Instant
import java.nio.charset.StandardCharsets.UTF_8

import cats.Show
import cats.effect.Sync
import tofu.logging.Loggable
import vulcan.Codec
import io.circe.{Decoder, Encoder}
import tethys._
import tethys.jackson._
import tethys.readers.ReaderError
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

  object Raw extends TaggedType[Array[Byte]] {
    implicit final class Ops(private val value: Type) extends AnyVal {
      def jsonAs[R: JsonReader]: Either[ReaderError, R] = new String(value, UTF_8).jsonAs[R]
    }

    implicit val loggable: Loggable[Type]                          = Loggable.empty
    implicit val vulcanCodec: Codec[Type]                          = Codec.bytes.asInstanceOf[Codec[Type]]
    implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, Type] = EntityDecoder.byteArrayDecoder[F].asInstanceOf[EntityDecoder[F, Type]]
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
}
