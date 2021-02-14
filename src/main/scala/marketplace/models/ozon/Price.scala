package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable, decoder)
final case class Price(price: Price.Value, finalPrice: Price.Value, discount: Price.Percent)

object Price {
  object Value extends TaggedType[Double] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Value = Value.Type

  object Percent extends TaggedType[Byte] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Percent = Percent.Type

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Price): FreeApplicative[Codec.Field[A, *], Price] =
    (field("priceBase", f(_).price), field("priceFinal", f(_).finalPrice), field("pricePercentDiscount", f(_).discount))
      .mapN(apply)
}
