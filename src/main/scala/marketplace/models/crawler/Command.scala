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

@derive(loggable)
sealed trait CrawlerCommand extends Command

object CrawlerCommand {

  @derive(loggable)
  case class HandleOzonRequest(id: Command.Id, key: Command.Key, created: Timestamp, request: OzonRequest) extends CrawlerCommand

  def handleOzonRequest[F[_]: FlatMap: Clock: GenUUID](request: OzonRequest): F[CrawlerCommand] =
    for {
      uuid    <- GenUUID[F].randomUUID
      instant <- Clock[F].instantNow
      key      = request.url.path
    } yield HandleOzonRequest(uuid @@ Command.Id, key @@ Command.Key, instant @@ Timestamp, request)

  object HandleOzonRequest {
    implicit val vulcanCodec: Codec[HandleOzonRequest] =
      Codec.record[HandleOzonRequest](name = "HandleOzonRequest", namespace = "crawler.commands")(fb =>
        (fb("id", _.id), fb("key", _.key), fb("created", _.created), fb("request", _.request)).mapN(apply)
      )
  }

  implicit val vulcanCodec: Codec[CrawlerCommand] = Codec.union[CrawlerCommand](alt => alt[HandleOzonRequest])
}
