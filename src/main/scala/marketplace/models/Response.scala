package marketplace.models

import derevo.derive
import tofu.logging.derivation.loggable

@derive(loggable)
final case class Response(headers: Headers, bodyText: String)
