package net.dalytics.models.aggregator

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec

import net.dalytics.models._

@derive(loggable)
sealed trait AggregatorCommand extends Command

object AggregatorCommand {

  @derive(loggable)
  final case class AggregateOzonSearchResultsV2Item private (
    created: Timestamp,
    timestamp: Timestamp,
    category: ozon.Category,
    page: ozon.Page,
    item: ozon.Item
  ) extends AggregatorCommand

  object AggregateOzonSearchResultsV2Item {
    implicit val vulcanCodec: Codec[AggregateOzonSearchResultsV2Item] =
      Codec.record[AggregateOzonSearchResultsV2Item](
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

  implicit val vulcanCodec: Codec[AggregatorCommand] = Codec.union[AggregatorCommand](alt => alt[AggregateOzonSearchResultsV2Item])
}
