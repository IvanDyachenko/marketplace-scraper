package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}
import vulcan.Codec
import vulcan.generic._
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
@AvroNamespace("ozon.models")
final case class Catalog(category: Category, totalFound: Int, totalPages: Int, currentPage: Int, searchResultsV2: SearchResultsV2)

object Catalog {

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  implicit def circeDecoder(layout: Layout): Decoder[Catalog] = new Decoder[Catalog] {
    final def apply(c: HCursor): Decoder.Result[Catalog] = {
      lazy val i = c.downField("shared").downField("catalog")
      for {
        category        <- i.get[Category]("category")
        totalFound      <- i.get[Int]("totalFound")
        totalPages      <- i.get[Int]("totalPages")
        currentPage     <- i.get[Int]("currentPage")
        searchResultsV2 <- layout.searchResultsV2.fold[Decoder.Result[SearchResultsV2]](
                             Left(
                               DecodingFailure(
                                 "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                                 c.history
                               )
                             )
                           )(component => c.downField("searchResultsV2").get[SearchResultsV2](component.stateId))
      } yield Catalog(category, totalFound, totalPages, currentPage, searchResultsV2)
    }
  }

  implicit val vulcanCodec: Codec[Catalog] = Codec.derive[Catalog]
}
