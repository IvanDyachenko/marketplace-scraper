package marketplace.models.crawler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.generate.GenUUID
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{Command, Timestamp}
import marketplace.models.ozon.{Request => OzonRequest}
import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait CrawlerCommand extends Command

@derive(loggable)
case class HandleOzonRequest(id: Command.Id, key: Command.Key, created: Timestamp, request: OzonRequest) extends CrawlerCommand

@derive(loggable)
case class HandleYandexMarketRequest(id: Command.Id, key: Command.Key, created: Timestamp, request: YandexMarketRequest)
    extends CrawlerCommand

object CrawlerCommand {
  def handleOzonRequest[F[_]: FlatMap: Clock: GenUUID](request: OzonRequest): F[CrawlerCommand] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.url.path
    } yield HandleOzonRequest(uuid @@ Command.Id, key @@ Command.Key, Timestamp(instant), request)

  def handleYandexMarketRequest[F[_]: FlatMap: Clock: GenUUID](request: YandexMarketRequest): F[CrawlerCommand] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.method
    } yield HandleYandexMarketRequest(uuid @@ Command.Id, key @@ Command.Key, Timestamp(instant), request)

  implicit val vulcanCodec: Codec[CrawlerCommand] =
    Codec.union[CrawlerCommand](alt => alt[HandleOzonRequest] |+| alt[HandleYandexMarketRequest])
}

object HandleOzonRequest {
  implicit val vulcanCodec: Codec[HandleOzonRequest] =
    Codec.record[HandleOzonRequest]("HandleOzonRequest", "crawler.commands")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("request", _.request)).mapN(apply)
    )
}

object HandleYandexMarketRequest {
  implicit val vulcanCodec: Codec[HandleYandexMarketRequest] =
    Codec.record[HandleYandexMarketRequest]("HandleYandexMarketRequest", "crawler.commands")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("request", _.request)).mapN(apply)
    )
}
