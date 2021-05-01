package net.dalytics.models.handler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.{loggable, unembed}
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models.{ozon, Command, Timestamp}

@derive(loggable)
sealed trait HandlerCommand extends Command

object HandlerCommand {

  @derive(loggable)
  final case class HandleOzonRequest(created: Timestamp, @unembed request: ozon.Request) extends HandlerCommand {
    override val key: Option[Command.Key] = Some(request.url.path @@ Command.Key)
  }

  object HandleOzonRequest {
    implicit val vulcanCodec: Codec[HandleOzonRequest] =
      Codec.record[HandleOzonRequest](
        name = "HandleOzonRequest",
        namespace = "handler.commands"
      )(field => (field("_created", _.created), field("request", _.request)).mapN(apply))
  }

  def handleOzonRequest[F[_]: FlatMap: Clock](request: ozon.Request): F[HandlerCommand] =
    for {
      instant <- Clock[F].instantNow
    } yield HandleOzonRequest(instant @@ Timestamp, request)

  implicit val vulcanCodec: Codec[HandlerCommand] = Codec.union[HandlerCommand](alt => alt[HandleOzonRequest])
}
