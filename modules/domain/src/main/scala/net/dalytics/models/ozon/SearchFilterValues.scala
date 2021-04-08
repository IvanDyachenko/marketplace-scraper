package net.dalytics.models.ozon

import derevo.derive
import derevo.circe.decoder
import io.circe.{Decoder, HCursor}
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase

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
  sealed trait Key extends EnumEntry with LowerCamelcase with Product with Serializable
  object Key       extends Enum[Key] with CatsEnum[Key] with CirceEnum[Key] with LoggableEnum[Key] {
    val values = findValues

    case object Brand extends Key
  }

  implicit val circeDecoder: Decoder[SearchFilterValues] = Decoder.instance { (c: HCursor) =>
    for {
      key                <- c.get[Key]("key")
      decoder             = key match {
                              case Key.Brand => Decoder[SearchFilterBrands]
                            }
      searchFilterValues <- decoder(c)
    } yield searchFilterValues
  }
}
