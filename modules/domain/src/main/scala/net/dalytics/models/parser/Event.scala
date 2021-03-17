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
  final case class OzonSellerListItemParsed private (
    created: Timestamp,
    timestamp: Timestamp,
    item: ozon.MarketplaceSeller
  ) extends ParserEvent {
    override val key: Option[Event.Key] = Some(item.title @@@ Event.Key)
  }

  object OzonSellerListItemParsed {
    def apply[F[_]: Monad: Clock](timestamp: Timestamp, sellerList: ozon.SellerList.Success): F[List[ParserEvent]] = {
      val ozon.SellerList.Success(items) = sellerList
      items.traverse(item => Clock[F].instantNow.map(instant => OzonSellerListItemParsed(instant @@ Timestamp, timestamp, item)))
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
    category: ozon.Category,
    page: ozon.Page,
    item: ozon.Item
  ) extends ParserEvent {
    override val key: Option[Event.Key] = Some(category.name @@@ Event.Key)
  }

  object OzonCategorySearchResultsV2ItemParsed {
    def apply[F[_]: Monad: Clock](timestamp: Timestamp, searchResultsV2: ozon.CategorySearchResultsV2.Success): F[List[ParserEvent]] = {
      val ozon.CategorySearchResultsV2.Success(category, page, items) = searchResultsV2
      items.traverse { item =>
        Clock[F].instantNow.map(instant => OzonCategorySearchResultsV2ItemParsed(instant @@ Timestamp, timestamp, category, page, item))
      }
    }

    implicit val vulcanCodec: Codec[OzonCategorySearchResultsV2ItemParsed] =
      Codec.record[OzonCategorySearchResultsV2ItemParsed](
        name = "OzonCategorySearchResultsV2ItemParsed",
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

  implicit val vulcanCodec: Codec[ParserEvent] =
    Codec.union[ParserEvent](alt => alt[OzonSellerListItemParsed] |+| alt[OzonCategorySearchResultsV2ItemParsed])
}
