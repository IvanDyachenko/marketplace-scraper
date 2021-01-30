package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import vulcan.Codec
import vulcan.generic._
import supertagged.TaggedType

import marketplace.syntax._
import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
sealed trait Component {
  def stateId: Component.StateId
}

object Component {
  object StateId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type StateId = StateId.Type

  @derive(loggable)
  @AvroNamespace("ozon.models.layout.components")
  final case class UWidgetSKU(stateId: Component.StateId) extends Component

  @derive(loggable)
  @AvroNamespace("ozon.models.layout.components")
  final case class SearchResultsV2(stateId: Component.StateId) extends Component

  object UWidgetSKU {
    implicit val vulcanCodec: Codec[UWidgetSKU] = Codec.derive[UWidgetSKU]
  }

  object SearchResultsV2 {
    implicit val vulcanCodec: Codec[SearchResultsV2] = Codec.derive[SearchResultsV2]
  }

  implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("component"))
  implicit val circeDecoder: Decoder[Component]  = deriveConfiguredDecoder[Component]

  implicit val vulcanCodec: Codec[Component] = Codec.union[Component](alt => alt[UWidgetSKU] |+| alt[SearchResultsV2])
}
