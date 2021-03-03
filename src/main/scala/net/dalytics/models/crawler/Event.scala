package net.dalytics.models.crawler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Json
import vulcan.Codec
import supertagged.postfix._

import net.dalytics.models._

@derive(loggable)
sealed trait CrawlerEvent extends Event

object CrawlerEvent {

  @derive(loggable)
  final case class OzonRequestHandled private (created: Timestamp, raw: Json) extends CrawlerEvent

  def ozonRequestHandled[F[_]: FlatMap: Clock](raw: Json): F[CrawlerEvent] =
    for {
      instant <- Clock[F].instantNow
    } yield OzonRequestHandled(instant @@ Timestamp, raw)

  object OzonRequestHandled {
    implicit val vulcanCodec: Codec[OzonRequestHandled] =
      Codec.record[OzonRequestHandled](
        name = "OzonRequestHandled",
        namespace = "crawler.events"
      )(field => (field("_created", _.created), field("raw", _.raw)).mapN(apply))
  }

  implicit val vulcanCodec: Codec[CrawlerEvent] = Codec.union[CrawlerEvent](alt => alt[OzonRequestHandled])
}
