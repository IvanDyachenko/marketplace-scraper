package marketplace.models

import java.nio.charset.StandardCharsets.UTF_8

import cats.implicits._
import io.circe.Json
import io.circe.parser.decode
import vulcan.{AvroError, Codec}
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.Loggable

@derive(loggable)
sealed trait CrawlerEvent {
  def id: EventId
  def created: Timestamp
}

@derive(loggable)
final case class YandexMarketRequestHandled(id: EventId, created: Timestamp, raw: Json) extends CrawlerEvent

object CrawlerEvent {
  implicit val vulcanCodec: Codec[CrawlerEvent] =
    Codec.union[CrawlerEvent](alt => alt[YandexMarketRequestHandled])
}

object YandexMarketRequestHandled {
  implicit val jsonLoggable: Loggable[Json] = Loggable.empty
  implicit val jsonCodec: Codec[Json]       = Codec.bytes
    .imapError(bytes => decode[Json](new String(bytes, UTF_8)).left.map(err => AvroError(err.getMessage)))(_.noSpaces.getBytes(UTF_8))

  implicit val vulcanCodec: Codec[YandexMarketRequestHandled] =
    Codec.record[YandexMarketRequestHandled]("YandexMarketRequestHandled", "marketplace.models")(field =>
      (field("id", _.id), field("created", _.created), field("raw", _.raw)).mapN(YandexMarketRequestHandled.apply)
    )
}
