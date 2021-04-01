package net.dalytics.models.ozon

import io.circe.{Decoder, HCursor}

final case class Result(
  cms: Option[Cms],
  catalog: Option[Catalog]
) {
  def sellerList: Option[SellerList]                                     = cms.flatMap(_.sellerList)
  def categoryMenu: Option[CategoryMenu]                                 = catalog.flatMap(_.categoryMenu)
  def searchResultsV2: Option[SearchResultsV2]                           = catalog.flatMap(_.searchResultsV2)
  def categorySearchResultsV2: Option[(Page, Category, SearchResultsV2)] =
    for {
      page            <- catalog.map(_.page)
      category        <- catalog.map(_.category)
      searchResultsV2 <- searchResultsV2
    } yield (page, category, searchResultsV2)
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
