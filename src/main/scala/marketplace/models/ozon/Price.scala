package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic._
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable, decoder)
@AvroNamespace("ozon.models")
final case class Price(price: Int, finalPrice: Int, discount: Price.Percent)

object Price {
  object Percent extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Percent = Percent.Type

  implicit val vulcanCodec: Codec[Price] = Codec.derive[Price]

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Price): FreeApplicative[Codec.Field[A, *], Price] =
    (
      field("priceBase", f.andThen(_.price)),
      field("priceFinal", f.andThen(_.finalPrice)),
      field("pricePercentDiscount", f.andThen(_.discount))
    )
      .mapN(apply)
}
