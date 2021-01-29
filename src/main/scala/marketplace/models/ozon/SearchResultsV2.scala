package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.{loggable, masked, MaskMode}
import vulcan.Codec
import io.circe.Decoder

@derive(loggable)
sealed trait SearchResultsV2

object SearchResultsV2 {

  @derive(loggable, decoder)
  final case class Success(@masked(MaskMode.ForLength(0, 50)) items: List[Item]) extends SearchResultsV2

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SearchResultsV2

  object Success {
    implicit val vulcanCodec: Codec[Success] =
      Codec.record[Success](name = "SearchResultsV2", namespace = "ozon.models")(field => field("items", _.items).map(apply))
  }

  object Failure {
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
