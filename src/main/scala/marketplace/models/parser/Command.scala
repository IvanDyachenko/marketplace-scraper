package marketplace.models.parser

import java.nio.charset.StandardCharsets.UTF_8

import cats.implicits._
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable
import io.circe.Json
import io.circe.parser.decode
import vulcan.{AvroError, Codec}

import marketplace.models.{Command, Timestamp}

@derive(loggable)
sealed trait ParserCommand extends Command

@derive(loggable)
case class ParseYandexMarketResponse(id: Command.Id, key: Command.Key, created: Timestamp, response: Json) extends ParserCommand

object ParserCommand {
  implicit val vulcanCodec: Codec[ParserCommand] =
    Codec.union[ParserCommand](alt => alt[ParseYandexMarketResponse])
}

object ParseYandexMarketResponse {
  implicit val jsonLoggable: Loggable[Json] = Loggable.empty
  implicit val jsonCodec: Codec[Json]       = Codec.bytes
    .imapError(bytes => decode[Json](new String(bytes, UTF_8)).left.map(err => AvroError(err.getMessage)))(_.noSpaces.getBytes(UTF_8))

  implicit val vulcanCodec: Codec[ParseYandexMarketResponse] =
    Codec.record[ParseYandexMarketResponse]("ParseYandexMarketResponse", "parser.commands")(field =>
      (field("id", _.id), field("key", _.key), field("created", _.created), field("response", _.response))
        .mapN(ParseYandexMarketResponse.apply)
    )
}
