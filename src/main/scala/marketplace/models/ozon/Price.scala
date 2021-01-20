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
final case class Price(price: Int, finalPrice: Int, discount: Price.Percent)

object Price {
  object Percent extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Percent = Percent.Type

  def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Price): FreeApplicative[Codec.Field[A, *], Price] =
    (
      field("priceValue", f.andThen(_.price)),
      field("priceFinal", f.andThen(_.finalPrice)),
      field("pricePercentDiscount", f.andThen(_.discount))
    )
      .mapN(apply)
}
