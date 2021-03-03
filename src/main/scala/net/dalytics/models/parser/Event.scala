package net.dalytics.models.parser

import cats.implicits._
import cats.Monad
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models.{ozon, Event, Timestamp}

@derive(loggable)
sealed trait ParserEvent extends Event {
  def timestamp: Timestamp
}

object ParserEvent {

  @derive(loggable)
  final case class OzonSearchResultsV2ItemParsed private (
    created: Timestamp,
    timestamp: Timestamp,
    category: ozon.Category,
    page: ozon.Page,
    item: ozon.Item
  ) extends ParserEvent {
    override val key: Option[Event.Key] = Some(category.name @@@ Event.Key)
  }

  object OzonSearchResultsV2ItemParsed {

    def apply[F[_]: Monad: Clock](timestamp: Timestamp, searchResultsV2: ozon.SearchResultsV2.Success): F[List[ParserEvent]] = {
      val ozon.SearchResultsV2.Success(category, page, items) = searchResultsV2
      items.traverse(item => Clock[F].instantNow.map(instant => OzonSearchResultsV2ItemParsed(instant @@ Timestamp, timestamp, category, page, item)))
    }

    implicit val vulcanCodec: Codec[OzonSearchResultsV2ItemParsed] =
      Codec.record[OzonSearchResultsV2ItemParsed](
        name = "OzonSearchResultsV2ItemParsed",
        namespace = "parser.events"
      ) { field =>
        (
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
