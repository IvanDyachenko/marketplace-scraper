package net.dalytics.models.aggregator

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models.{ozon, Event, Timestamp}

sealed trait AggregatorEvent extends Event {
  def timestamp: Timestamp
}

object AggregatorEvent {

  @derive(loggable)
  final case class OzonSearchResultsV2ItemAggregated(
    created: Timestamp,
    timestamp: Timestamp,
    category: ozon.Category,
    page: ozon.Page,
    item: ozon.Item,
    sale: ozon.Sale
  ) extends AggregatorEvent {
    override val key: Option[Event.Key] = Some(category.name @@@ Event.Key)
  }

  object OzonSearchResultsV2ItemAggregated {

    implicit val vulcanCodec: Codec[OzonSearchResultsV2ItemAggregated] =
      Codec.record[OzonSearchResultsV2ItemAggregated](
        name = "OzonSearchResultsV2ItemAggregated",
        namespace = "aggregator.events"
      ) { field =>
        (
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.Category.vulcanCodecFieldFA(field)(_.category),
          ozon.Page.vulcanCodecFieldFA(field)(_.page),
          ozon.Item.vulcanCodecFieldFA(field)(_.item),
          ozon.Sale.vulcanCodecFieldFA(field)(_.sale)
        ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[AggregatorEvent] = Codec.union[AggregatorEvent](alt => alt[OzonSearchResultsV2ItemAggregated])
}
