package marketplace.models

import derevo.derive
import tofu.logging.derivation.loggable

@derive(loggable)
sealed trait Response

final case class DummyResponse(you: String) extends Response
