package net.dalytics.models.ozon

import derevo.derive
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import tethys.JsonReader
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import supertagged.TaggedType

import net.dalytics.syntax._
import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

@derive(loggable)
sealed trait Component {
  def stateId: Component.StateId
}

object Component {
  object StateId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type StateId = StateId.Type

  //@derive(loggable)
  //final case class UWidgetSKU(stateId: Component.StateId) extends Component

  @derive(loggable, tethysReader)
  final case class SellerList(stateId: Component.StateId) extends Component

  @derive(loggable, tethysReader)
  final case class CategoryMenu(stateId: Component.StateId) extends Component

  @derive(loggable, tethysReader)
  final case class SearchResultsV2(stateId: Component.StateId) extends Component

  @derive(loggable, tethysReader)
  final case class SoldOutResultsV2(stateId: Component.StateId) extends Component

  implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("component"))
  implicit val circeDecoder: Decoder[Component]  = deriveConfiguredDecoder[Component]

  implicit val jsonReader: JsonReader[Component] = JsonReader.builder.addField[String]("component").selectReader {
    case "sellerList"       => JsonReader[SellerList]
    case "categoryMenu"     => JsonReader[CategoryMenu]
    case "searchResultsV2"  => JsonReader[SearchResultsV2]
    case "soldOutResultsV2" => JsonReader[SoldOutResultsV2]
  }
}
