package marketplace.models

import java.nio.charset.StandardCharsets.UTF_8

import io.circe.Json
import io.circe.parser.decode
import tofu.logging.Loggable
import vulcan.{AvroError, Codec}

package object crawler {
  implicit val jsonLoggable: Loggable[Json] = Loggable.empty
  implicit val jsonVulcanCodec: Codec[Json] = Codec.bytes
    .imapError(bytes => decode[Json](new String(bytes, UTF_8)).left.map(err => AvroError(err.getMessage)))(_.noSpaces.getBytes(UTF_8))
}
