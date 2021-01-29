package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Catalog(category: Category, searchResultsV2: SearchResultsV2, totalFound: Int, totalPages: Int, currentPage: Int)

object Catalog {

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  implicit val circeDecoder: Decoder[Catalog] = new Decoder[Catalog] {
    final def apply(c: HCursor): Decoder.Result[Catalog] =
      for {
        layout          <- c.downField("layout").as[Layout]
        i                = c.downField("catalog").downField("shared").downField("catalog")
        category        <- i.get[Category]("category")
        searchResultsV2 <- layout.searchResultsV2.fold[Decoder.Result[SearchResultsV2]](
                             Left(
                               DecodingFailure(
                                 "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                                 c.history
                               )
                             )
                           )(component => c.downField("catalog").downField("searchResultsV2").get[SearchResultsV2](component.stateId))
        totalFound      <- i.get[Int]("totalFound")
        totalPages      <- i.get[Int]("totalPages")
        currentPage     <- i.get[Int]("currentPage")
      } yield Catalog(category, searchResultsV2, totalFound, totalPages, currentPage)
  }
}
