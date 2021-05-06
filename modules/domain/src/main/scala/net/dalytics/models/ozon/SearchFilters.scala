package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import tethys.JsonReader

@derive(loggable)
sealed trait SearchFilters {
  type T <: SearchFilter

  def values: List[T]
}

@derive(loggable, decoder, tethysReader)
final case class BrandFilters(values: List[BrandFilter]) extends SearchFilters {
  type T = BrandFilter
}

object SearchFilters {
  implicit val circeDecoder: Decoder[SearchFilters] = Decoder.instance { (c: HCursor) =>
    for {
      key           <- c.get[SearchFilter.Key]("key")
      decoder        = key match {
                         case SearchFilter.Key.Brand => Decoder[BrandFilters].widen
                       }
      searchFilters <- decoder(c)
    } yield searchFilters
  }

  implicit val jsonReader: JsonReader[SearchFilters] =
    JsonReader.builder
      .addField[SearchFilter.Key]("key")
      .selectReader { case SearchFilter.Key.Brand => JsonReader[BrandFilters] }
}
