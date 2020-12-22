package marketplace.models.crawler

import java.nio.charset.StandardCharsets.UTF_8

import supertagged.postfix._
import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import tofu.generate.GenUUID
import io.circe.Json
import io.circe.parser.decode
import vulcan.{AvroError, Codec}
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

import marketplace.models.{EventId, EventKey, Timestamp}
import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait Event {
  def id: EventId
  def key: EventKey
  def created: Timestamp
}

@derive(loggable)
final case class YandexMarketRequestHandled(id: EventId, key: EventKey, created: Timestamp, raw: Json) extends Event

object Event {
  def yandexMarketRequestHandled[F[_]: FlatMap: Clock: GenUUID](request: YandexMarketRequest, raw: Json): F[Event] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.method
    } yield YandexMarketRequestHandled(uuid @@ EventId, key @@ EventKey, Timestamp(instant), raw)

  implicit val vulcanCodec: Codec[Event] =
    Codec.union[Event](alt => alt[YandexMarketRequestHandled])
}

object YandexMarketRequestHandled {
  implicit val jsonLoggable: Loggable[Json] = Loggable.empty
  implicit val jsonCodec: Codec[Json]       = Codec.bytes
    .imapError(bytes => decode[Json](new String(bytes, UTF_8)).left.map(err => AvroError(err.getMessage)))(_.noSpaces.getBytes(UTF_8))

  implicit val vulcanCodec: Codec[YandexMarketRequestHandled] =
    Codec.record[YandexMarketRequestHandled]("YandexMarketRequestHandled", "crawler.events")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("raw", _.raw)).mapN(YandexMarketRequestHandled.apply)
    )
}
