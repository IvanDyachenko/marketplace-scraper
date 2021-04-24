package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder
import tethys.JsonReader
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

@derive(loggable)
final case class Brand(id: Brand.Id, name: Brand.Name)

object Brand {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Name = Name.Type

  implicit val circeDecoder: Decoder[Brand]  = Decoder.forProduct2("brandId", "brand")(apply)
  implicit val jsonReader: JsonReader[Brand] = JsonReader.builder.addField[Id]("brandId").addField[Name]("brand").buildReader(apply)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Brand): FreeApplicative[Codec.Field[A, *], Brand] =
    (field("brandId", f(_).id), field("brandName", f(_).name)).mapN(apply)
}
