package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.{Camelcase, Uppercase}
import io.circe.Decoder
import tethys.JsonReader
import tethys.enumeratum.TethysEnum
import vulcan.Codec
import vulcan.generic.AvroNamespace

@derive(loggable)
final case class Delivery(schema: Delivery.Schema, timeDiffDays: Short)

object Delivery {
  def apply(schema: Delivery.Schema, timeDiffDays: Option[Short]): Delivery = apply(schema, timeDiffDays.getOrElse(0: Short))

  @AvroNamespace("ozon.models.delivery")
  sealed trait Schema extends EnumEntry with Camelcase with Product with Serializable
  object Schema
      extends Enum[Schema]
      with CatsEnum[Schema]
      with CirceEnum[Schema]
      with TethysEnum[Schema]
      with VulcanEnum[Schema]
      with LoggableEnum[Schema] {
    val values = findValues

    case object FBO         extends Schema with Uppercase // Товар продается со склада Ozon.
    case object FBS         extends Schema with Uppercase // Товар продается со склада продавца.
    case object Retail      extends Schema                // Товар продает сам Ozon. Товар продается со склада Ozon.
    case object Crossborder extends Schema                // Трансграничная торговля.
  }

  implicit val circeDecoder: Decoder[Delivery] = Decoder.forProduct2[Delivery, Schema, Option[Short]]("deliverySchema", "deliveryTimeDiffDays")(apply)

  implicit val jsonReader: JsonReader[Delivery] =
    JsonReader.builder
      .addField[Schema]("deliverySchema")
      .addField[Option[Short]]("deliveryTimeDiffDays")
      .buildReader(apply)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Delivery): FreeApplicative[Codec.Field[A, *], Delivery] =
    (field("deliverySchema", f(_).schema), field("deliveryTimeDiffDays", f(_).timeDiffDays)).mapN(apply)
}
