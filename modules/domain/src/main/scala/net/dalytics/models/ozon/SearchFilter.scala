package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase

import net.dalytics.models.ozon.Brand.{Id => BrandId}

object SearchFilter {
  sealed trait Key extends EnumEntry with LowerCamelcase with Product with Serializable
  object Key       extends Enum[Key] with CatsEnum[Key] with CirceEnum[Key] with LoggableEnum[Key] {
    val values = findValues

    case object Brand extends Key
  }

  @derive(loggable)
  sealed trait Value

  @derive(loggable)
  final case class Brand(brandId: BrandId) extends Value

  object Brand {
    implicit val circeDecoder: Decoder[Brand] = Decoder.forProduct1("key")(apply)
  }

  implicit val valueCirceDecoder: Decoder[Value] =
    List[Decoder[Value]](
      Decoder[Brand].widen
    ).reduceLeft(_ or _)

  @derive(loggable)
  sealed trait Values {
    type T <: Value
    def values: List[T]
  }

  @derive(loggable, decoder)
  final case class Brands(values: List[Brand]) extends Values {
    type T = Brand
  }

  implicit val valuesCirceDecoder: Decoder[Values] = Decoder.instance { (c: HCursor) =>
    for {
      key    <- c.get[Key]("key")
      decoder = key match {
                  case Key.Brand => Decoder[Brands]
                }
      values <- decoder(c)
    } yield values
  }
}
