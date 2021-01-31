package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import vulcan.Codec
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
sealed trait SearchResultsV2

object SearchResultsV2 {

  @derive(loggable)
  final case class Success(@masked(MaskMode.ForLength(0, 50)) items: List[Item]) extends SearchResultsV2

  @derive(loggable)
  final case class Failure(error: String) extends SearchResultsV2

  object Success {
    implicit val circeDecoder: Decoder[Success] = new Decoder[Success] {
      final def apply(c: HCursor): Decoder.Result[Success] = {
        lazy val i = c.downField("shared").downField("catalog")

        for {
          page     <- c.get[Page]("page")
          layout   <- c.get[Layout]("layout")
          category <- i.get[Category]("category")
          items    <- layout.searchResultsV2.fold[Decoder.Result[List[Item]]](
                        Left(
                          DecodingFailure(
                            "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                            c.history
                          )
                        )
                      ) { component =>
                        c.downField("searchResultsV2").get[List[Item]](component.stateId)(Decoder.decodeList[Item](Item.circeDecoder(category, page)))
                      }
        } yield Success(items)
      }
    }

    implicit val vulcanCodec: Codec[Success] =
      Codec.record[Success](name = "SearchResultsV2", namespace = "ozon.models")(field => field("items", _.items).map(apply))
  }

  object Failure {
    implicit val circeDecoder: Decoder[Failure] = new Decoder[Failure] {
      final def apply(c: HCursor): Decoder.Result[Failure] = {
        lazy val i = c.downField("shared").downField("catalog")

        for {
          layout <- c.get[Layout]("layout")
          error  <- layout.searchResultsV2.fold[Decoder.Result[String]](
                      Left(
                        DecodingFailure(
                          "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                          c.history
                        )
                      )
                    )(component => c.downField("searchResultsV2").downField(component.stateId).get[String]("error"))
        } yield Failure(error)
      }
    }

    implicit val vulcanCodec: Codec[Failure] =
      Codec.record[Failure](name = "FailureSearchResultsV2", namespace = "ozon.models")(_("error", _.error).map(apply))
  }

  implicit val circeDecoder: Decoder[SearchResultsV2] =
    List[Decoder[SearchResultsV2]](
      Decoder[Failure].widen,
      Decoder[Success].widen
    ).reduceLeft(_ or _)

  implicit val vulcanCodec: Codec[SearchResultsV2] = Codec.union[SearchResultsV2](alt => alt[Success] |+| alt[Failure])
}
