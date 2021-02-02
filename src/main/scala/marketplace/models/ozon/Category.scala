package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Category(id: Category.Id, name: Category.Name, catalogName: Option[Catalog.Name]) {
  val link: Url = Url(s"/category/${id.show}")
}

object Category {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  object Path extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Path = Path.Type

  implicit val circeDecoder: Decoder[Category] = Decoder.forProduct3("id", "name", "catalogName")(apply)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Category): FreeApplicative[Codec.Field[A, *], Category] =
    (field("categoryId", f(_).id), field("categoryName", f(_).name), field("categoryCatalogName", f(_).catalogName)).mapN(apply)
}
