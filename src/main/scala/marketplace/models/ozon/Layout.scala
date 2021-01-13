package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder
import marketplace.models.ozon.Component.SearchResultsV2

@derive(loggable)
final case class Layout(components: List[Component])

object Layout {
  implicit class LayoutOps(layout: Layout) {
    val searchResultsV2: Option[Component.SearchResultsV2] =
      layout.components.collectFirst { case component @ SearchResultsV2(_) => component }
  }

  implicit val circeDecoder: Decoder[Layout] = Decoder.decodeList[Component].map(apply)
}
