package marketplace.models

import cats.implicits._
import vulcan.Codec
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait CrawlerCommand {
  def id: CommandId
  def created: Timestamp
}

@derive(loggable)
case class HandleYandexMarketRequest(id: CommandId, created: Timestamp, request: YandexMarketRequest) extends CrawlerCommand

object CrawlerCommand {
  implicit val vulcanCodec: Codec[CrawlerCommand] =
    Codec.union[CrawlerCommand](alt => alt[HandleYandexMarketRequest])
}

object HandleYandexMarketRequest {
  implicit val vulcanCodec: Codec[HandleYandexMarketRequest] =
    Codec.record[HandleYandexMarketRequest]("HandleYandexMarketRequest", "marketplace.models")(field =>
      (field("id", _.id), field("created", _.created), field("request", _.request)).mapN(HandleYandexMarketRequest.apply)
    )
}
