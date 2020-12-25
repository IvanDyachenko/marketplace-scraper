package marketplace.models.crawler

import supertagged.postfix._
import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import tofu.generate.GenUUID
import io.circe.Json
import vulcan.Codec
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{EventId, EventKey, Timestamp}
import marketplace.models.ozon.{Request => OzonRequest}
import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait Event {
  def id: EventId
  def key: EventKey
  def created: Timestamp
}

@derive(loggable)
final case class OzonRequestHandled(id: EventId, key: EventKey, created: Timestamp, raw: Json) extends Event

@derive(loggable)
final case class YandexMarketRequestHandled(id: EventId, key: EventKey, created: Timestamp, raw: Json) extends Event

object Event {
  def ozonRequestHandled[F[_]: FlatMap: Clock: GenUUID](request: OzonRequest, raw: Json): F[Event] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.path
    } yield OzonRequestHandled(uuid @@ EventId, key @@ EventKey, Timestamp(instant), raw)

  def yandexMarketRequestHandled[F[_]: FlatMap: Clock: GenUUID](request: YandexMarketRequest, raw: Json): F[Event] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.method
    } yield YandexMarketRequestHandled(uuid @@ EventId, key @@ EventKey, Timestamp(instant), raw)

  implicit val vulcanCodec: Codec[Event] =
    Codec.union[Event](alt => alt[OzonRequestHandled] |+| alt[YandexMarketRequestHandled])
}

object OzonRequestHandled {
  implicit val vulcanCodec: Codec[OzonRequestHandled] =
    Codec.record[OzonRequestHandled]("OzonRequestHandled", "crawler.events")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("raw", _.raw)).mapN(apply)
    )
}

object YandexMarketRequestHandled {
  implicit val vulcanCodec: Codec[YandexMarketRequestHandled] =
    Codec.record[YandexMarketRequestHandled]("YandexMarketRequestHandled", "crawler.events")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("raw", _.raw)).mapN(apply)
    )
}
