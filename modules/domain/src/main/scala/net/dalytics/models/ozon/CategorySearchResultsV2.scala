package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
sealed trait CategorySearchResultsV2 extends Result

object CategorySearchResultsV2 {

  @derive(loggable)
  final case class Success(category: Category, page: Page, items: List[Item]) extends CategorySearchResultsV2

  @derive(loggable, decoder)
  final case class Failure(error: String) extends CategorySearchResultsV2

  object Success {
    implicit def circeDecoder(category: Category, page: Page): Decoder[Success] =
      Decoder.forProduct1("items")(items => Success(category, page, items))
  }

  implicit val circeDecoder: Decoder[CategorySearchResultsV2] = new Decoder[CategorySearchResultsV2] {
    final def apply(c: HCursor): Decoder.Result[CategorySearchResultsV2] =
      for {
        layout          <- c.get[Layout]("layout")
        i                = c.downField("catalog").downField("shared").downField("catalog")
        category        <- i.get[Category]("category")
        page            <- i.as[Page]
        searchResultsV2 <- layout.searchResultsV2.fold[Decoder.Result[CategorySearchResultsV2]](
                             Left(
                               DecodingFailure("'layout' object doesn't contain component which corresponds to 'searchResultsV2'", c.history)
                             )
                           ) { component =>
                             val circeDecoder = List[Decoder[CategorySearchResultsV2]](
                               Decoder[Failure].widen,
                               Success.circeDecoder(category, page).widen
                             ).reduceLeft(_ or _)

                             c.downField("catalog")
                               .downField("searchResultsV2")
                               .downField(component.stateId)
                               .as[CategorySearchResultsV2](circeDecoder)
                           }
      } yield searchResultsV2
  }
}
