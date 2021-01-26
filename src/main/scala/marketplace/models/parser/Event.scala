package marketplace.models.parser

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.generate.GenUUID
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{Event, Timestamp}
import marketplace.models.ozon.{Item => OzonItem, Result => OzonResult}

@derive(loggable)
sealed trait ParserEvent extends Event {
  def time: Timestamp
}

object ParserEvent {

  @derive(loggable)
  final case class OzonItemParsed(id: Event.Id, key: Event.Key, created: Timestamp, time: Timestamp, item: OzonItem) extends ParserEvent

  @derive(loggable)
  final case class OzonResponseParsed(id: Event.Id, key: Event.Key, created: Timestamp, time: Timestamp, result: OzonResult) extends ParserEvent

  def ozonItemParsed(id: Event.Id, key: Event.Key, created: Timestamp, time: Timestamp, item: OzonItem): ParserEvent =
    OzonItemParsed(id, key, created, time, item)

  def ozonResponseParsed[F[_]: FlatMap: Clock: GenUUID](time: Timestamp, result: OzonResult): F[ParserEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
    } yield OzonResponseParsed(uuid @@ Event.Id, "ozon" @@ Event.Key, instant @@ Timestamp, time, result)

  object OzonItemParsed {
    implicit val vulcanCodec: Codec[OzonItemParsed] =
      Codec.record[OzonItemParsed](name = "OzonItemParsed", namespace = "parser.events") { fb =>
        (fb("_id", _.id), fb("_key", _.key), fb("_created", _.created), fb("time", _.time), OzonItem.vulcanCodecFieldFA(fb)(_.item)).mapN(apply)
      }
  }

  object OzonResponseParsed {
    implicit val vulcanCodec: Codec[OzonResponseParsed] =
      Codec.record[OzonResponseParsed](name = "OzonResponseParsed", namespace = "parser.events") { fb =>
        (fb("_id", _.id), fb("_key", _.key), fb("_created", _.created), fb("time", _.time), fb("result", _.result)).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[ParserEvent] = Codec.union[ParserEvent](alt => alt[OzonItemParsed] |+| alt[OzonResponseParsed])
}
