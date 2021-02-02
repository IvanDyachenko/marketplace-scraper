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
import marketplace.models.ozon.{Item => OzonItem}

@derive(loggable)
sealed trait ParserEvent extends Event {
  def timestamp: Timestamp
}

object ParserEvent {

  @derive(loggable)
  final case class OzonItemParsed(id: Event.Id, key: Event.Key, created: Timestamp, timestamp: Timestamp, result: OzonItem) extends ParserEvent

  def ozonItemParsed[F[_]: FlatMap: Clock: GenUUID](timestamp: Timestamp, item: OzonItem): F[ParserEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
    } yield OzonItemParsed(uuid @@ Event.Id, item.catalog.category.name @@@ Event.Key, instant @@ Timestamp, timestamp, item)

  object OzonItemParsed {
    implicit val vulcanCodec: Codec[OzonItemParsed] =
      Codec.record[OzonItemParsed](name = "OzonItemParsed", namespace = "parser.events") { field =>
        (
          field("_id", _.id),
          field("_key", _.key),
          field("_created", _.created),
          field("timestamp", _.timestamp),
          OzonItem.vulcanCodecFieldFA(field)(_.result)
        ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[ParserEvent] = Codec.union[ParserEvent](alt => alt[OzonItemParsed])
}
