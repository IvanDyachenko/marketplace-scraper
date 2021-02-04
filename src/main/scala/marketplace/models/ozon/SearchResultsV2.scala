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
  final case class Success(@masked(MaskMode.ForLength(0, 50)) items: List[Item]) extends SearchResultsV2

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SearchResultsV2

  object Success {
    implicit def circeDecoder(category: Category): Decoder[Success] =
      Decoder.forProduct1("items")(apply)(Decoder.decodeList[Item](Item.circeDecoder(category)))
  }

  implicit val circeDecoder: Decoder[SearchResultsV2] = new Decoder[SearchResultsV2] {
    final def apply(c: HCursor): Decoder.Result[SearchResultsV2] = {
      lazy val i = c.downField("catalog")

      for {
        layout          <- c.get[Layout]("layout")
        category        <- i.downField("shared").get[Category]("catalog")
        searchResultsV2 <- layout.searchResultsV2.fold[Decoder.Result[SearchResultsV2]](
                             Left(
                               DecodingFailure(
                                 "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                                 c.history
                               )
                             )
                           ) { component =>
                             val circeDecoder =
                               List[Decoder[SearchResultsV2]](
                                 Decoder[Failure].widen,
                                 Success.circeDecoder(category).widen
                               ).reduceLeft(_ or _)

                             i.downField("searchResultsV2").downField(component.stateId).as[SearchResultsV2](circeDecoder)
                           }
      } yield searchResultsV2
    }
  }

}
