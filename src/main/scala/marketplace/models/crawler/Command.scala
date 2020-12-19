package marketplace.models.crawler

import cats.implicits._
import vulcan.Codec
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{CommandId, CommandKey, Timestamp}
import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait Command {
  def id: CommandId
  def key: CommandKey
  def created: Timestamp
}

@derive(loggable)
case class HandleYandexMarketRequest(id: CommandId, key: CommandKey, created: Timestamp, request: YandexMarketRequest) extends Command

object Command {
  implicit val vulcanCodec: Codec[Command] =
    Codec.union[Command](alt => alt[HandleYandexMarketRequest])
}

object HandleYandexMarketRequest {
  implicit val vulcanCodec: Codec[HandleYandexMarketRequest] =
    Codec.record[HandleYandexMarketRequest]("HandleYandexMarketRequest", "crawler.commands")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("request", _.request))
        .mapN(HandleYandexMarketRequest.apply)
    )
}