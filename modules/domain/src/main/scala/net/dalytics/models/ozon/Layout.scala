package net.dalytics.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, Json}

@derive(loggable)
final case class Layout(components: List[Component]) {
  def sellerList: Option[Component.SellerList]           = components.collectFirst { case c @ Component.SellerList(_) => c }
  def categoryMenu: Option[Component.CategoryMenu]       = components.collectFirst { case c @ Component.CategoryMenu(_) => c }
  def searchResultsV2: Option[Component.SearchResultsV2] = components.collectFirst { case c @ Component.SearchResultsV2(_) => c }
}

object Layout {
  implicit val circeDecoder: Decoder[Layout] =
    Decoder
      .decodeList(Decoder[Component].either(Decoder[Json]))
      .map(ls => Layout(ls.flatMap(_.left.toOption)))
}
