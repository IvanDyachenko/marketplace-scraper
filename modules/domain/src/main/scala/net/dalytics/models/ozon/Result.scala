package net.dalytics.models.ozon

import io.circe.{Decoder, HCursor}
import tethys.JsonReader
import tethys.derivation.semiauto._
import tethys.derivation.builder.ReaderBuilder

final case class Result(cms: Option[Cms], catalog: Option[Catalog]) {
  def page: Option[Page]                         = catalog.map(_.page)
  def category: Option[Category]                 = catalog.map(_.category)
  def categoryMenu: Option[CategoryMenu]         = catalog.flatMap(_.categoryMenu)
  def searchResultsV2: Option[SearchResultsV2]   = catalog.flatMap(_.searchResultsV2)
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

  implicit val tethysJsonReader: JsonReader[Result] = jsonReader[Result] {
    describe {
      ReaderBuilder[Result]
        .extractReader[Option[Cms]](_.cms)
        .from("layout".as[Layout])(layout => JsonReader.optionReader(Cms.tethysJsonReader(layout)))
        .extractReader[Option[Catalog]](_.catalog)
        .from("layout".as[Layout])(layout => JsonReader.optionReader(Catalog.tethysJsonReader(layout)))
    }
  }
}
