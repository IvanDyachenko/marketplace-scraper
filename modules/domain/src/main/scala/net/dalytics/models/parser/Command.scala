package net.dalytics.models.parser

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.Json
import vulcan.Codec

import net.dalytics.models.{jsonLoggable, jsonVulcanCodec, Command, Timestamp}

@derive(loggable)
sealed trait ParserCommand extends Command

object ParserCommand {

  @derive(loggable)
  final case class ParseOzonResponse private (created: Timestamp, @masked(MaskMode.Erase) response: Json) extends ParserCommand

  object ParseOzonResponse {
    implicit val vulcanCodec: Codec[ParseOzonResponse] =
      Codec.record[ParseOzonResponse](
        name = "ParseOzonResponse",
        namespace = "parser.commands",
        aliases = Seq("handler.events.OzonRequestHandled")
      )(field => (field("_created", _.created), field("response", _.response, aliases = Seq("raw"))).mapN(apply))
  }

  implicit val vulcanCodec: Codec[ParserCommand] = Codec.union[ParserCommand](alt => alt[ParseOzonResponse])
}
