package marketplace.clients

import derevo.derive
import tofu.logging.derivation.loggable

@derive(loggable)
sealed abstract class HttpClientError(message: String) extends Exception(message, null, false, true)

@derive(loggable)
final case class HttpClientDecodingError(bodyText: String) extends HttpClientError(bodyText)
