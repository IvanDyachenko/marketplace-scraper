package marketplace.models.crawler

import supertagged.postfix._
import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import tofu.generate.GenUUID
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
  def handleYandexMarketRequest[F[_]: FlatMap: Clock: GenUUID](request: YandexMarketRequest): F[Command] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.method
    } yield HandleYandexMarketRequest(uuid @@ CommandId, key @@ CommandKey, Timestamp(instant), request)

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
