package marketplace.models

import derevo.derive
import tofu.logging.derivation.loggable
import java.time.Instant
import vulcan.Codec

@derive(loggable)
case class Timestamp(value: Instant)

object Timestamp {
  implicit val vulcanCodec: Codec[Timestamp] = Codec.instant.imap(apply)(_.value)
}
