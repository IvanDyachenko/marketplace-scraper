package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
sealed trait Component {
  def stateId: Component.StateId
}

object Component {
  object StateId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type StateId = StateId.Type

  @derive(loggable)
  final case class UWidgetSKU(stateId: Component.StateId) extends Component

  @derive(loggable)
  final case class SearchResultsV2(stateId: Component.StateId) extends Component

  implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, decapitalize, false, Some("component"))
  implicit val circeDecoder: Decoder[Component]  = deriveConfiguredDecoder[Component]

  private def decapitalize(s: String): String =
    if (s == null || s.length == 0 || s.charAt(0).isLower) s
    else s.updated(0, s.charAt(0).toLower)
}
