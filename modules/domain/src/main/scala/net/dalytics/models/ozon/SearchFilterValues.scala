package net.dalytics.models.ozon

import derevo.derive
import derevo.circe.decoder
import io.circe.{Decoder, HCursor}
import tofu.logging.derivation.loggable

@derive(loggable)
sealed trait SearchFilterValues {
  type T <: SearchFilterValue
  def values: List[T]
}

@derive(loggable, decoder)
final case class SearchFilterBrands(values: List[SearchFilterBrand]) extends SearchFilterValues {
  type T = SearchFilterBrand
}

object SearchFilterValues {
  implicit val circeDecoder: Decoder[SearchFilterValues] = Decoder.instance { (c: HCursor) =>
    for {
      key                <- c.get[SearchFilterKey]("key")
      decoder             = key match {
                              case SearchFilterKey.Brand => Decoder[SearchFilterBrands]
                            }
      searchFilterValues <- decoder(c)
    } yield searchFilterValues
  }
}
