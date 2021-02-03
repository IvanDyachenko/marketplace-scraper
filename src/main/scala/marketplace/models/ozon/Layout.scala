package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder

@derive(loggable)
final case class Layout(components: List[Component])

object Layout {
  implicit final class LayoutOps(private val layout: Layout) extends AnyVal {
    def categoryMenu: Option[Component.CategoryMenu]       = layout.components.collectFirst { case c @ Component.CategoryMenu(_) => c }
    def searchResultsV2: Option[Component.SearchResultsV2] = layout.components.collectFirst { case c @ Component.SearchResultsV2(_) => c }
  }

  implicit val circeDecoder: Decoder[Layout] = Decoder.decodeList[Component].map(apply)
}
