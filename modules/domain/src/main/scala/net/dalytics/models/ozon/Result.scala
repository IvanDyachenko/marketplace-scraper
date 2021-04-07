package net.dalytics.models.ozon

import io.circe.{Decoder, HCursor}

final case class Result(
  cms: Option[Cms],
  catalog: Option[Catalog]
) {
  def sellerList: Option[SellerList]                                       = cms.flatMap(_.sellerList)
  def page: Option[Page]                                                   = catalog.map(_.page)
  def category: Option[Category]                                           = catalog.map(_.category)
  def categoryMenu: Option[CategoryMenu]                                   = catalog.flatMap(_.categoryMenu)
  def searchResultsV2: Option[SearchResultsV2]                             = catalog.flatMap(_.searchResultsV2)
  def soldOutResultsV2: Option[SoldOutResultsV2]                           = catalog.flatMap(_.soldOutResultsV2)
  def categorySearchResultsV2: Option[(Page, Category, SearchResultsV2)]   =
    for {
      page            <- page
      category        <- category
      searchResultsV2 <- searchResultsV2
    } yield (page, category, searchResultsV2)
  def categorySoldOutResultsV2: Option[(Page, Category, SoldOutResultsV2)] =
    for {
      page             <- page
      category         <- category
      soldOutResultsV2 <- soldOutResultsV2
    } yield (page, category, soldOutResultsV2)
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
