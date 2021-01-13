package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
sealed trait Result

object Result {

  @derive(loggable)
  final case class SearchResultsV2(@masked(MaskMode.Erase) items: List[Item]) extends Result

  @derive(loggable)
  final case class FailureSearchResultsV2(error: String) extends Result

  object SearchResultsV2 {
    implicit val circeDecoder: Decoder[SearchResultsV2] = ???
  }

  object FailureSearchResultsV2 {
    implicit val circeDecoder: Decoder[FailureSearchResultsV2] = new Decoder[FailureSearchResultsV2] {
      final def apply(c: HCursor): Decoder.Result[FailureSearchResultsV2] =
        for {
          layout                 <- c.downField("layout").as[Layout]
          failureSearchResultsV2 <-
            layout.searchResultsV2.fold[Decoder.Result[FailureSearchResultsV2]](
              Left(DecodingFailure("layout field doesn't contain searchResultsV2 component", c.history))
            ) { component =>
              for {
                error <- c.downField("catalog").downField("searchResultsV2").downField(component.stateId).get[String]("error")
              } yield FailureSearchResultsV2(error)
            }
        } yield failureSearchResultsV2
    }
  }

  implicit val circeDecoder: Decoder[Result] =
    List[Decoder[Result]](
      Decoder[SearchResultsV2].widen,
      Decoder[FailureSearchResultsV2].widen
    ).reduceLeft(_ or _)
}
