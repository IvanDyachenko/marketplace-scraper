package marketplace.models.crawler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.generate.GenUUID
import io.circe.Json
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{Event, Timestamp}
import marketplace.models.ozon.{Request => OzonRequest}

@derive(loggable)
sealed trait CrawlerEvent extends Event

object CrawlerEvent {
  @derive(loggable)
  final case class OzonRequestHandled(id: Event.Id, key: Event.Key, created: Timestamp, raw: Json) extends CrawlerEvent

  def ozonRequestHandled[F[_]: FlatMap: Clock: GenUUID](request: OzonRequest, raw: Json): F[CrawlerEvent] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.url.path
    } yield OzonRequestHandled(uuid @@ Event.Id, key @@ Event.Key, instant @@ Timestamp, raw)

  object OzonRequestHandled {
    implicit val vulcanCodec: Codec[OzonRequestHandled] =
      Codec.record[OzonRequestHandled](
        name = "OzonRequestHandled",
        namespace = "crawler.events"
      ) { field =>
        (field("id", _.id), field("key", _.key), field("created", _.created), field("raw", _.raw)).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[CrawlerEvent] =
    Codec.union[CrawlerEvent](alt => alt[OzonRequestHandled])
}
