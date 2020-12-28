package marketplace.models.parser

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.generate.GenUUID
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{Event, Timestamp}
import marketplace.models.yandex.market.{Result => YandexMarketResult}

@derive(loggable)
sealed trait ParserEvent extends Event

@derive(loggable)
final case class YandexMarketResponseParsed(id: Event.Id, key: Event.Key, created: Timestamp, result: YandexMarketResult)
    extends ParserEvent

object ParserEvent {
  def yandexMarketResponseParsed[F[_]: FlatMap: Clock: GenUUID](result: YandexMarketResult): F[ParserEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
    } yield YandexMarketResponseParsed(uuid @@ Event.Id, "yandex.market" @@ Event.Key, Timestamp(instant), result)

  implicit val vulcanCodec: Codec[ParserEvent] =
    Codec.union[ParserEvent](alt => alt[YandexMarketResponseParsed])
}

object YandexMarketResponseParsed {
  implicit val vulcanCodec: Codec[YandexMarketResponseParsed] =
    Codec.record[YandexMarketResponseParsed]("YandexMarketResponseHandled", "parser.events")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("result", _.result))
        .mapN(YandexMarketResponseParsed.apply)
    )
}
