package net.dalytics.models.ozon

import io.circe.{Decoder, HCursor}

final case class Result(
  cms: Option[Cms],
  catalog: Option[Catalog]
) {
  def page: Option[Page]                       = catalog.map(_.page)
  def category: Option[Category]               = catalog.map(_.category)
  def sellerList: Option[SellerList]           = cms.flatMap(_.sellerList)
  def categoryMenu: Option[CategoryMenu]       = catalog.flatMap(_.categoryMenu)
  def searchResultsV2: Option[SearchResultsV2] = catalog.flatMap(_.searchResultsV2)
}

object Result {
  implicit val circeDecoder: Decoder[Result] = Decoder.instance[Result] { (c: HCursor) =>
    for {
      layout  <- c.get[Layout]("layout")
      cms     <- c.getOrElse[Option[Cms]]("cms")(None)(Cms.circeDecoder(layout).map(Some(_)))
      catalog <- c.getOrElse[Option[Catalog]]("catalog")(None)(Catalog.circeDecoder(layout).map(Some(_)))
    } yield Result(cms, catalog)
  }
}
