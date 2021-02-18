package marketplace.models.crawler

import cats.implicits._
import cats.FlatMap
import cats.effect.Clock
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.postfix._

import marketplace.models.{Command, Timestamp}
import marketplace.models.ozon.{Request => OzonRequest}

@derive(loggable)
sealed trait CrawlerCommand extends Command

object CrawlerCommand {

  @derive(loggable)
  final case class HandleOzonRequest private (created: Timestamp, request: OzonRequest) extends CrawlerCommand {
    val key: Command.Key = request.url.path @@ Command.Key
  }

  def handleOzonRequest[F[_]: FlatMap: Clock](request: OzonRequest): F[CrawlerCommand] =
    for {
      instant <- Clock[F].instantNow
    } yield HandleOzonRequest(instant @@ Timestamp, request)

  object HandleOzonRequest {
    implicit val vulcanCodec: Codec[HandleOzonRequest] =
      Codec.record[HandleOzonRequest](
        name = "HandleOzonRequest",
        namespace = "crawler.commands"
      )(field => (field("_created", _.created), field("request", _.request)).mapN(apply))
  }

  implicit val vulcanCodec: Codec[CrawlerCommand] = Codec.union[CrawlerCommand](alt => alt[HandleOzonRequest])
}
