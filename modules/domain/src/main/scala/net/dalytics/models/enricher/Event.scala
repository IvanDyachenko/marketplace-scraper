package net.dalytics.models.aggregator

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models.{ozon, Event, Timestamp}

sealed trait EnricherEvent extends Event {
  def timestamp: Timestamp
}

object EnricherEvent {

  @derive(loggable)
  final case class OzonCategorySearchResultsV2ItemEnriched(
    created: Timestamp,
    timestamp: Timestamp,
    page: ozon.Page,
    item: ozon.Item,
    sale: ozon.Sale,
    category: ozon.Category
  ) extends EnricherEvent {
    override val key: Option[Event.Key] = Some(category.name @@@ Event.Key)
  }

  object OzonCategorySearchResultsV2ItemEnriched {

    implicit val vulcanCodec: Codec[OzonCategorySearchResultsV2ItemEnriched] =
      Codec.record[OzonCategorySearchResultsV2ItemEnriched](
        name = "OzonCategorySearchResultsV2ItemEnriched",
        namespace = "enricher.events"
      ) { field =>
        (
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.Page.vulcanCodecFieldFA(field)(_.page),
          ozon.Item.vulcanCodecFieldFA(field)(_.item),
          ozon.Sale.vulcanCodecFieldFA(field)(_.sale),
          ozon.Category.vulcanCodecFieldFA(field)(_.category)
        ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[EnricherEvent] = Codec.union[EnricherEvent](alt => alt[OzonCategorySearchResultsV2ItemEnriched])
}
