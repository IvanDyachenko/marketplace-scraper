package net.dalytics.models.handler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.{loggable, masked, unembed, MaskMode}
import io.circe.Json
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models.{jsonLoggable, jsonVulcanCodec, Event, Timestamp}

@derive(loggable)
sealed trait HandlerEvent extends Event

object HandlerEvent {

  @derive(loggable)
  final case class OzonRequestHandled(created: Timestamp, @unembed @masked(MaskMode.Erase) raw: Json) extends HandlerEvent

  def ozonRequestHandled[F[_]: FlatMap: Clock](raw: Json): F[HandlerEvent] =
    for {
      instant <- Clock[F].instantNow
    } yield OzonRequestHandled(instant @@ Timestamp, raw)

  object OzonRequestHandled {
    implicit val vulcanCodec: Codec[OzonRequestHandled] =
      Codec.record[OzonRequestHandled](
        name = "OzonRequestHandled",
        namespace = "handler.events"
      )(field => (field("_created", _.created), field("raw", _.raw)).mapN(apply))
  }

  implicit val vulcanCodec: Codec[HandlerEvent] = Codec.union[HandlerEvent](alt => alt[OzonRequestHandled])
}
