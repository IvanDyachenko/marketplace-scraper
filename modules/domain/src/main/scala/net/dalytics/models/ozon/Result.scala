package net.dalytics.models.ozon

import io.circe.{Decoder, HCursor}

final case class Result(cms: Option[Cms], catalog: Option[Catalog]) {
  def category: Option[Category]                 = catalog.map(_.category)
  def categoryMenu: Option[CategoryMenu]         = catalog.flatMap(_.categoryMenu)
  def searchPage: Option[SearchPage]             = catalog.flatMap(_.searchPage)
  def searchResultsV2: Option[SearchResultsV2]   = catalog.flatMap(_.searchResultsV2)
  def soldOutPage: Option[SoldOutPage]           = catalog.flatMap(_.soldOutPage)
  def soldOutResultsV2: Option[SoldOutResultsV2] = catalog.flatMap(_.soldOutResultsV2)
  def sellerList: Option[SellerList]             = cms.flatMap(_.sellerList)
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
