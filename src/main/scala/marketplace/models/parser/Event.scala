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
import marketplace.models.ozon.{Result => OzonResult}

@derive(loggable)
sealed trait ParserEvent extends Event

object ParserEvent {
  @derive(loggable)
  final case class OzonResponseParsed(id: Event.Id, key: Event.Key, created: Timestamp, result: OzonResult) extends ParserEvent

  def ozonResponseParsed[F[_]: FlatMap: Clock: GenUUID](result: OzonResult): F[ParserEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
    } yield OzonResponseParsed(uuid @@ Event.Id, "ozon" @@ Event.Key, instant @@ Timestamp, result)

  object OzonResponseParsed {
    implicit val vulcanCodec: Codec[OzonResponseParsed] =
      Codec.record[OzonResponseParsed]("OzonResponseParsed", "parser.events")(field =>
        (field("id", _.id), field("key", _.key), field("created", _.created), field("result", _.result)).mapN(apply)
      )
  }

  implicit val vulcanCodec: Codec[ParserEvent] =
    Codec.union[ParserEvent](alt => alt[OzonResponseParsed])
}
