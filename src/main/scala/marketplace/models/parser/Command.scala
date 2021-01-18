package marketplace.models.parser

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Json
import vulcan.Codec

import marketplace.models.{Command, Timestamp}

@derive(loggable)
sealed trait ParserCommand extends Command

@derive(loggable)
final case class ParseOzonResponse(id: Command.Id, key: Command.Key, created: Timestamp, response: Json) extends ParserCommand

@derive(loggable)
final case class ParseYandexMarketResponse(id: Command.Id, key: Command.Key, created: Timestamp, response: Json) extends ParserCommand

object ParserCommand {
  implicit val vulcanCodec: Codec[ParserCommand] =
    Codec.union[ParserCommand](alt => alt[ParseOzonResponse] |+| alt[ParseYandexMarketResponse])
}

object ParseOzonResponse {
  implicit val vulcanCodec: Codec[ParseOzonResponse] =
    Codec.record[ParseOzonResponse]("ParseOzonResponse", "parser.commands")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("response", _.response))
        .mapN(ParseOzonResponse.apply)
    )
}

object ParseYandexMarketResponse {
  implicit val vulcanCodec: Codec[ParseYandexMarketResponse] =
    Codec.record[ParseYandexMarketResponse]("ParseYandexMarketResponse", "parser.commands")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("response", _.response))
        .mapN(ParseYandexMarketResponse.apply)
    )
}
