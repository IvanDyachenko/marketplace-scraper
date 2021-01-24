package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic._
import io.circe.Decoder
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
@AvroNamespace("ozon.models")
final case class Brand(id: Brand.Id, name: Brand.Name)

object Brand {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  implicit val circeDecoder: Decoder[Brand] = Decoder.forProduct2("brandId", "brand")(apply)
  implicit val vulcanCodec: Codec[Brand]    = Codec.derive[Brand]

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Brand): FreeApplicative[Codec.Field[A, *], Brand] =
    (field("brandId", f(_).id), field("brandName", f(_).name)).mapN(apply)
}
