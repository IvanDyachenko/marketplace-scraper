package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
sealed trait SearchResultsV2

object SearchResultsV2 {

  @derive(loggable)
  final case class Success(category: Category, page: Page, @masked(MaskMode.ForLength(0, 50)) items: List[Item]) extends SearchResultsV2

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SearchResultsV2

  object Success {
    implicit def circeDecoder(category: Category, page: Page): Decoder[Success] =
      Decoder.forProduct1("items")(items => Success(category, page, items))
  }

  implicit val circeDecoder: Decoder[SearchResultsV2] = new Decoder[SearchResultsV2] {
    final def apply(c: HCursor): Decoder.Result[SearchResultsV2] =
      for {
        layout          <- c.get[Layout]("layout")
        i                = c.downField("catalog").downField("shared").downField("catalog")
        category        <- i.get[Category]("category")
        page            <- i.as[Page]
        searchResultsV2 <- layout.searchResultsV2.fold[Decoder.Result[SearchResultsV2]](
                             Left(
                               DecodingFailure(
                                 "\"layout\" object doesn't contain component with \"component\" which is corresponds to \"searchResultsV2\"",
                                 c.history
                               )
                             )
                           ) { component =>
                             val circeDecoder =
                               List[Decoder[SearchResultsV2]](Decoder[Failure].widen, Success.circeDecoder(category, page).widen).reduceLeft(_ or _)

                             c.downField("catalog").downField("searchResultsV2").downField(component.stateId).as[SearchResultsV2](circeDecoder)
                           }
      } yield searchResultsV2
  }
}
