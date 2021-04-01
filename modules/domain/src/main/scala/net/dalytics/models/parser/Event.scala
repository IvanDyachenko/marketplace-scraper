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
  final case class OzonSellerListItemParsed private (created: Timestamp, timestamp: Timestamp, item: ozon.MarketplaceSeller) extends ParserEvent {
    override val key: Option[Event.Key] = Some(item.id.show @@ Event.Key)
  }

  object OzonSellerListItemParsed {
    def apply[F[_]: Monad: Clock](timestamp: Timestamp, sellerList: ozon.SellerList): F[List[ParserEvent]] =
      sellerList match {
        case ozon.SellerList.Success(items) =>
          for {
            created <- Clock[F].instantNow.map(_ @@ Timestamp)
            events   = items.map(OzonSellerListItemParsed(created, timestamp, _))
          } yield events
        case _                              => List.empty[ParserEvent].pure[F]
      }

    implicit val vulcanCodec: Codec[OzonSellerListItemParsed] =
      Codec.record[OzonSellerListItemParsed](
        name = "OzonSellerListItemParsed",
        namespace = "parser.events"
      ) { field =>
        (
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.MarketplaceSeller.vulcanCodecFieldFA(field)(_.item)
        ).mapN(apply)
      }
  }

  @derive(loggable)
  final case class OzonCategorySearchResultsV2ItemParsed private (
    created: Timestamp,
    timestamp: Timestamp,
    page: ozon.Page,
    item: ozon.Item,
    category: ozon.Category
  ) extends ParserEvent {
    override val key: Option[Event.Key] = Some(item.id.show @@ Event.Key)
  }

  object OzonCategorySearchResultsV2ItemParsed {
    def apply[F[_]: Monad: Clock](
      timestamp: Timestamp,
      page: ozon.Page,
      category: ozon.Category,
      searchResultsV2: ozon.SearchResultsV2
    ): F[List[ParserEvent]] =
      searchResultsV2 match {
        case ozon.SearchResultsV2.Success(items) =>
          for {
            created <- Clock[F].instantNow.map(_ @@ Timestamp)
            events   = items.map(OzonCategorySearchResultsV2ItemParsed(created, timestamp, page, _, category))
          } yield events
        case _                                   => List.empty[ParserEvent].pure[F]
      }

    implicit val vulcanCodec: Codec[OzonCategorySearchResultsV2ItemParsed] =
      Codec.record[OzonCategorySearchResultsV2ItemParsed](
        name = "OzonCategorySearchResultsV2ItemParsed",
        namespace = "parser.events",
        aliases = Seq("parser.events.OzonSearchResultsV2ItemParsed")
      ) { field =>
        (
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.Page.vulcanCodecFieldFA(field)(_.page),
          ozon.Item.vulcanCodecFieldFA(field)(_.item),
          ozon.Category.vulcanCodecFieldFA(field)(_.category)
        ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[ParserEvent] =
    Codec.union[ParserEvent](alt => alt[OzonSellerListItemParsed] |+| alt[OzonCategorySearchResultsV2ItemParsed])
}
