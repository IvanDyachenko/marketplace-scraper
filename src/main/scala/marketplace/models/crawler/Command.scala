package marketplace.models.crawler

import cats.implicits._
import vulcan.Codec
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{CommandId, Timestamp}
import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait Command {
  def id: CommandId
  def created: Timestamp
}

@derive(loggable)
case class HandleYandexMarketRequest(id: CommandId, created: Timestamp, request: YandexMarketRequest) extends Command

object Command {
  implicit val vulcanCodec: Codec[Command] =
    Codec.union[Command](alt => alt[HandleYandexMarketRequest])
}

object HandleYandexMarketRequest {
  implicit val vulcanCodec: Codec[HandleYandexMarketRequest] =
    Codec.record[HandleYandexMarketRequest]("HandleYandexMarketRequest", "marketplace.models.crawler")(field =>
      (field("id", _.id), field("created", _.created), field("request", _.request)).mapN(HandleYandexMarketRequest.apply)
    )
}
