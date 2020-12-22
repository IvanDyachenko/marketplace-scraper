package marketplace.models.parser

import cats.implicits._
import vulcan.Codec
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{EventId, EventKey, Timestamp}
import marketplace.models.yandex.market.{Result => YandexMarketResult}

@derive(loggable)
sealed trait Event {
  def id: EventId
  def key: EventKey
  def created: Timestamp
}

@derive(loggable)
final case class YandexMarketResponseParsed(id: EventId, key: EventKey, created: Timestamp, result: YandexMarketResult) extends Event

object Event {
  implicit val vulcanCodec: Codec[Event] =
    Codec.union[Event](alt => alt[YandexMarketResponseParsed])
}

object YandexMarketResponseParsed {
  implicit val vulcanCodec: Codec[YandexMarketResponseParsed] =
    Codec.record[YandexMarketResponseParsed]("YandexMarketResponseHandled", "parser.events")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("result", _.result))
        .mapN(YandexMarketResponseParsed.apply)
    )
}
