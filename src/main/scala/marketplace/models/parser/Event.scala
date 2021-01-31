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
import marketplace.models.ozon.{SearchResultsV2 => OzonSearchResultsV2}

@derive(loggable)
sealed trait ParserEvent extends Event {
  def timestamp: Timestamp
}

object ParserEvent {

  @derive(loggable)
  final case class OzonResponseParsed(id: Event.Id, key: Event.Key, created: Timestamp, timestamp: Timestamp, result: OzonSearchResultsV2)
      extends ParserEvent

  def ozonResponseParsed[F[_]: FlatMap: Clock: GenUUID](timestamp: Timestamp, result: OzonSearchResultsV2): F[ParserEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
    } yield OzonResponseParsed(uuid @@ Event.Id, "searchResultsV2" @@ Event.Key, instant @@ Timestamp, timestamp, result)

  object OzonResponseParsed {
    implicit val vulcanCodec: Codec[OzonResponseParsed] =
      Codec.record[OzonResponseParsed](name = "OzonResponseParsed", namespace = "parser.events") { fb =>
        (fb("_id", _.id), fb("_key", _.key), fb("_created", _.created), fb("timestamp", _.timestamp), fb("result", _.result)).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[ParserEvent] = Codec.union[ParserEvent](alt => alt[OzonResponseParsed])
}
