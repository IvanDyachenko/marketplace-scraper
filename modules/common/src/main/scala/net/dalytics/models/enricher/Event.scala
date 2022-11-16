package net.dalytics.models.enricher

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.TaggedType
import supertagged.postfix._

import net.dalytics.models.{ozon, Event, LiftedCats, LiftedLoggable, LiftedVulcanCodec, Timestamp}

sealed trait EnricherEvent extends Event {
  def key: EnricherEvent.Key
  def timestamp: Timestamp
}

object EnricherEvent {
  object Key extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedVulcanCodec
  type Key = Key.Type

  @derive(loggable)
  final case class OzonCategoryResultsV2ItemEnriched(
    created: Timestamp,
    timestamp: Timestamp,
    page: ozon.Page,
    item: ozon.Item,
    category: ozon.Category,
    sale: ozon.Sale = ozon.Sale.Unknown
  ) extends EnricherEvent {
    val key: Key = category.id.show @@ Key

    def aggregate(that: OzonCategoryResultsV2ItemEnriched): OzonCategoryResultsV2ItemEnriched =
      if (timestamp.isBefore(that.timestamp)) that.aggregate(this)
      else OzonCategoryResultsV2ItemEnriched(created, timestamp, page, item, category, ozon.Sale.from(List(that.item, item)))
  }

  object OzonCategoryResultsV2ItemEnriched {
    implicit val vulcanCodec: Codec[OzonCategoryResultsV2ItemEnriched] =
      Codec.record[OzonCategoryResultsV2ItemEnriched](
        name = "OzonCategoryResultsV2ItemEnriched",
        namespace = "enricher.events"
      ) { field =>
        (
          field("_created", _.created),
          field("timestamp", _.timestamp),
          ozon.Page.vulcanCodecFieldFA(field)(_.page),
          ozon.Item.vulcanCodecFieldFA(field)(_.item),
          ozon.Category.vulcanCodecFieldFA(field)(_.category),
          ozon.Sale.vulcanCodecFieldFA(field)(_.sale)
        ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[EnricherEvent] = Codec.union[EnricherEvent](alt => alt[OzonCategoryResultsV2ItemEnriched])
}
