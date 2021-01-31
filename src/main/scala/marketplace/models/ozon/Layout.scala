package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder

import marketplace.models.ozon.Component.SearchResultsV2

@derive(loggable)
final case class Layout(components: List[Component])

object Layout {
  implicit final class LayoutOps(private val layout: Layout) extends AnyVal {
    def searchResultsV2: Option[Component.SearchResultsV2] =
      layout.components.collectFirst { case component @ SearchResultsV2(_) => component }
  }

  implicit val circeDecoder: Decoder[Layout] = Decoder.decodeList[Component].map(apply)
}
