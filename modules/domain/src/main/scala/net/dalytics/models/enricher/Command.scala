package net.dalytics.models.aggregator

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models.{ozon, Command, Timestamp}

@derive(loggable)
sealed trait EnricherCommand extends Command

object EnricherCommand {

  @derive(loggable)
  final case class EnrichOzonSellerListItem private (
    created: Timestamp,
    timestamp: Timestamp,
    item: ozon.MarketplaceSeller
  ) extends EnricherCommand {
    override val key: Option[Command.Key] = Some(item.title @@@ Command.Key)
  }

  object EnrichOzonSellerListItem {
    implicit val vulcanCodec: Codec[EnrichOzonSellerListItem] =
      Codec.record[EnrichOzonSellerListItem](
        name = "EnrichOzonSellerListItem",
        namespace = "enricher.commands",
        aliases = Seq("parser.events.OzonSellerListItemParsed")
      ) { field =>
        (
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.MarketplaceSeller.vulcanCodecFieldFA(field)(_.item)
        ).mapN(apply)
      }
  }

  @derive(loggable)
  final case class EnrichOzonCategorySearchResultsV2Item private (
    created: Timestamp,
    timestamp: Timestamp,
    page: ozon.Page,
    item: ozon.Item,
    category: ozon.Category
  ) extends EnricherCommand

  object EnrichOzonCategorySearchResultsV2Item {
    implicit val vulcanCodec: Codec[EnrichOzonCategorySearchResultsV2Item] =
      Codec.record[EnrichOzonCategorySearchResultsV2Item](
        name = "EnrichOzonCategorySearchResultsV2Item",
        namespace = "enricher.commands",
        aliases = Seq("parser.events.OzonCategorySearchResultsV2ItemParsed")
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

  implicit val vulcanCodec: Codec[EnricherCommand] =
    Codec.union[EnricherCommand](alt => alt[EnrichOzonSellerListItem] |+| alt[EnrichOzonCategorySearchResultsV2Item])
}
