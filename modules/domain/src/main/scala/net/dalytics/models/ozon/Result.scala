package net.dalytics.models.ozon

import cats.implicits._
import io.circe.Decoder

trait Result

object Result {
  implicit val circeDecoder: Decoder[Result] =
    List[Decoder[Result]](
      Decoder[SearchResultsV2].widen,
      Decoder[SellerList].widen
    ).reduceLeft(_ or _)
}
