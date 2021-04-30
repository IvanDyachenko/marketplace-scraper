package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import derevo.circe.decoder
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import vulcan.Codec
import tethys.JsonReader
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

@derive(loggable, decoder, tethysReader)
final case class Price(price: Price.Value, finalPrice: Price.Value, discount: Price.Percent)

object Price {
  object Value extends TaggedType[Double] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Value = Value.Type

  object Percent extends TaggedType[Byte] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit def jsonReader(implicit raw: JsonReader[Short]): JsonReader[Type] = raw.map(_.toByte).asInstanceOf[JsonReader[Type]]
  }
  type Percent = Percent.Type

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Price): FreeApplicative[Codec.Field[A, *], Price] =
    (field("priceBase", f(_).price), field("priceFinal", f(_).finalPrice), field("pricePercentDiscount", f(_).discount))
      .mapN(apply)
}
