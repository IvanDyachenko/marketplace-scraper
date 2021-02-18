package marketplace.models.parser

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Json
import vulcan.Codec
import supertagged.postfix._

import marketplace.models._

@derive(loggable)
sealed trait ParserCommand extends Command

object ParserCommand {

  @derive(loggable)
  final case class ParseOzonResponse private (created: Timestamp, response: Json) extends ParserCommand {
    val key: Command.Key = response.hashCode.toString @@ Command.Key
  }

  object ParseOzonResponse {
    implicit val vulcanCodec: Codec[ParseOzonResponse] =
      Codec.record[ParseOzonResponse](
        name = "ParseOzonResponse",
        namespace = "parser.commands",
        aliases = Seq("crawler.events.OzonRequestHandled")
      )(field => (field("_created", _.created), field("response", _.response, aliases = Seq("raw"))).mapN(apply))
  }

  implicit val vulcanCodec: Codec[ParserCommand] = Codec.union[ParserCommand](alt => alt[ParseOzonResponse])
}
