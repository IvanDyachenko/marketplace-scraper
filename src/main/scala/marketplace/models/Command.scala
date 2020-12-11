package marketplace.models

import cats.implicits._
import cats.Show
import supertagged.TaggedType
import vulcan.Codec
import tofu.logging.Loggable
import derevo.derive
import tofu.logging.derivation.loggable
import java.util.UUID

import marketplace.models.yandex.market.{Request => YandexMarketRequest}

@derive(loggable)
sealed trait Command {
  def id: Command.CommandId
  def created: Timestamp
}

@derive(loggable)
case class HandleYandexMarketRequest(id: Command.CommandId, created: Timestamp, request: YandexMarketRequest) extends Command

object HandleYandexMarketRequest {
  implicit val vulcanCodec: Codec[HandleYandexMarketRequest] =
    Codec.record[HandleYandexMarketRequest]("HandleYandexMarketRequest", "marketplace.models")(field =>
      (field("id", _.id), field("create", _.created), field("request", _.request)).mapN(HandleYandexMarketRequest.apply)
    )
}

object Command {
  object CommandId extends TaggedType[UUID] {
    implicit val show: Show[Type]         = Show.fromToString
    implicit val loggable: Loggable[Type] = Loggable.uuidLoggable.contramap(identity)
    implicit val vulcanCodec: Codec[Type] = lift
  }
  type CommandId = CommandId.Type

  implicit val vulcanCodec: Codec[Command] = Codec.union[Command](alt => alt[HandleYandexMarketRequest])
}
