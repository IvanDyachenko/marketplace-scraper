package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.{AvroNamespace, Codec}

@derive(loggable)
@AvroNamespace("ozon.models")
sealed trait Result

@derive(loggable)
final case class ItemsResult(
  @masked(MaskMode.Erase) items: List[Item]
) extends Result

object Result {
  implicit val circeDecoder: Decoder[Result] =
    List[Decoder[Result]](
      Decoder[ItemsResult].widen
    ).reduceLeft(_ or _)

  implicit val vulcanCodec: Codec[Result] =
    Codec.union[Result](alt => alt[ItemsResult])
}

object ItemsResult {
  implicit val circeDecoder: Decoder[ItemsResult] = deriveDecoder
  implicit val vulcanCodec: Codec[ItemsResult]    =
    Codec.record[ItemsResult]("ItemsResult", "ozon.models")(field => (field("items", _.items)).map(apply))
}
