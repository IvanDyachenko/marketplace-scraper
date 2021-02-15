package marketplace.models.parser

import cats.implicits._
import cats.Monad
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.generate.GenUUID
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{ozon, Event, Timestamp}

@derive(loggable)
sealed trait ParserEvent extends Event {
  def timestamp: Timestamp
}

object ParserEvent {

  @derive(loggable)
  final case class OzonSearchResultsV2ItemParsed private (
    id: Event.Id,
    key: Event.Key,
    created: Timestamp,
    timestamp: Timestamp,
    category: ozon.Category,
    page: ozon.Page,
    item: ozon.Item
  ) extends ParserEvent

  def ozonSearchResultsV2ItemParsed[F[_]: Monad: Clock: GenUUID](
    timestamp: Timestamp,
    searchResultsV2: ozon.SearchResultsV2.Success
  ): F[List[ParserEvent]] = {
    val ozon.SearchResultsV2.Success(category, page, items) = searchResultsV2

    items.traverse { item =>
      for {
        uuid    <- GenUUID[F].randomUUID
        instant <- Clock[F].instantNow
      } yield OzonSearchResultsV2ItemParsed(uuid @@ Event.Id, category.name @@@ Event.Key, instant @@ Timestamp, timestamp, category, page, item)
    }
  }

  object OzonSearchResultsV2ItemParsed {
    implicit val vulcanCodec: Codec[OzonSearchResultsV2ItemParsed] =
      Codec.record[OzonSearchResultsV2ItemParsed](name = "OzonSearchResultsV2ItemParsed", namespace = "parser.events") { field =>
        (
          field("_id", _.id),
          field("_key", _.key),
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.Category.vulcanCodecFieldFA(field)(_.category),
          ozon.Page.vulcanCodecFieldFA(field)(_.page),
          ozon.Item.vulcanCodecFieldFA(field)(_.item)
        ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[ParserEvent] = Codec.union[ParserEvent](alt => alt[OzonSearchResultsV2ItemParsed])
}
