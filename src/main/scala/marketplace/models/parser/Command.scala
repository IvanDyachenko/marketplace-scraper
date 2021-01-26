package marketplace.models.parser

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Json
import vulcan.Codec

import marketplace.models.{Command, Timestamp}

@derive(loggable)
sealed trait ParserCommand extends Command

object ParserCommand {

  @derive(loggable)
  final case class ParseOzonResponse(id: Command.Id, key: Command.Key, created: Timestamp, response: Json) extends ParserCommand

  object ParseOzonResponse {
    implicit val vulcanCodec: Codec[ParseOzonResponse] =
      Codec.record[ParseOzonResponse](name = "ParseOzonResponse", namespace = "parser.commands", aliases = Seq("crawler.events.OzonRequestHandled"))(
        fb => (fb("id", _.id), fb("key", _.key), fb("created", _.created), fb("response", _.response, aliases = Seq("raw"))).mapN(apply)
      )
  }

  implicit val vulcanCodec: Codec[ParserCommand] = Codec.union[ParserCommand](alt => alt[ParseOzonResponse])
}
