package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import vulcan.Codec
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
sealed trait Result

object Result {

  @derive(loggable)
  final case class SearchResultsV2(@masked(MaskMode.Erase) items: List[Item]) extends Result

  @derive(loggable)
  final case class FailureSearchResultsV2(error: ErrorDescr) extends Result

  object SearchResultsV2 {
    implicit val circeDecoder: Decoder[SearchResultsV2] = new Decoder[SearchResultsV2] {
      final def apply(c: HCursor): Decoder.Result[SearchResultsV2] =
        for {
          layout          <- c.downField("layout").as[Layout]
          searchResultsV2 <-
            layout.searchResultsV2.fold[Decoder.Result[SearchResultsV2]](
              Left(
                DecodingFailure(
                  "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                  c.history
                )
              )
            ) { component =>
              for {
                items <- c.downField("catalog").downField("searchResultsV2").downField(component.stateId).get[List[Item]]("items")
              } yield SearchResultsV2(items)
            }
        } yield searchResultsV2
    }

    implicit val vulcanCodec: Codec[SearchResultsV2] =
      Codec.record[SearchResultsV2](name = "SearchResultsV2", namespace = "ozon.models")(field => field("items", _.items).map(apply))
  }

  object FailureSearchResultsV2 {
    implicit val circeDecoder: Decoder[FailureSearchResultsV2] = new Decoder[FailureSearchResultsV2] {
      final def apply(c: HCursor): Decoder.Result[FailureSearchResultsV2] =
        for {
          layout                 <- c.downField("layout").as[Layout]
          failureSearchResultsV2 <-
            layout.searchResultsV2.fold[Decoder.Result[FailureSearchResultsV2]](
              Left(
                DecodingFailure(
                  "\"layout\" object doesn't contain component with \"component\" which is equal to \"searchResultsV2\"",
                  c.history
                )
              )
            ) { component =>
              for {
                error <- c.downField("catalog").downField("searchResultsV2").downField(component.stateId).as[ErrorDescr]
              } yield FailureSearchResultsV2(error)
            }
        } yield failureSearchResultsV2
    }

    implicit val vulcanCodec: Codec[FailureSearchResultsV2] =
      Codec.record[FailureSearchResultsV2](name = "FailureSearchResultsV2", namespace = "ozon.models")(_("error", _.error).map(apply))
  }

  implicit val circeDecoder: Decoder[Result] =
    List[Decoder[Result]](
      Decoder[FailureSearchResultsV2].widen,
      Decoder[SearchResultsV2].widen
    ).reduceLeft(_ or _)

  implicit val vulcanCodec: Codec[Result] = Codec.union[Result](alt => alt[SearchResultsV2] |+| alt[FailureSearchResultsV2])
}
