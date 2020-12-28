package marketplace.models.crawler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.generate.GenUUID
import io.circe.Json
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{Event, Timestamp}
import marketplace.models.ozon.{Request => OzonRequest}
import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait CrawlerEvent extends Event

@derive(loggable)
final case class OzonRequestHandled(id: Event.Id, key: Event.Key, created: Timestamp, raw: Json) extends CrawlerEvent

@derive(loggable)
final case class YandexMarketRequestHandled(id: Event.Id, key: Event.Key, created: Timestamp, raw: Json) extends CrawlerEvent

object CrawlerEvent {
  def ozonRequestHandled[F[_]: FlatMap: Clock: GenUUID](request: OzonRequest, raw: Json): F[CrawlerEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.url.path
    } yield OzonRequestHandled(uuid @@ Event.Id, key @@ Event.Key, Timestamp(instant), raw)

  def yandexMarketRequestHandled[F[_]: FlatMap: Clock: GenUUID](request: YandexMarketRequest, raw: Json): F[CrawlerEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.method
    } yield YandexMarketRequestHandled(uuid @@ Event.Id, key @@ Event.Key, Timestamp(instant), raw)

  implicit val vulcanCodec: Codec[CrawlerEvent] =
    Codec.union[CrawlerEvent](alt => alt[OzonRequestHandled] |+| alt[YandexMarketRequestHandled])
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
