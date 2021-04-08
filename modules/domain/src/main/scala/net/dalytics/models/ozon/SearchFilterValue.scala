package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Decoder

@derive(loggable)
sealed trait SearchFilterValue

@derive(loggable)
final case class SearchFilterBrand(brandId: Brand.Id) extends SearchFilterValue

object SearchFilterBrand {
  implicit val circeDecoder: Decoder[SearchFilterBrand] = Decoder.forProduct1("key")(apply)
}

object SearchFilterValue {

  implicit val circeDecoder: Decoder[SearchFilterValue] =
    List[Decoder[SearchFilterValue]](
      Decoder[SearchFilterBrand].widen
    ).reduceLeft(_ or _)
}
