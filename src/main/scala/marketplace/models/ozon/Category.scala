package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Category(id: Category.Id, name: Category.Name, catalogName: Catalog.Name, currentPage: Int, totalPages: Int, totalFoundItems: Int)

object Category {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  object Path extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Path = Path.Type

  implicit val circeDecoder: Decoder[Category] = new Decoder[Category] {
    final def apply(c: HCursor): Decoder.Result[Category] = {
      lazy val i = c.downField("category")

      (
        i.get[Id]("id"),
        i.get[Name]("name"),
        i.get[Catalog.Name]("catalogName"),
        c.get[Int]("currentPage"),
        c.get[Int]("totalPages"),
        c.get[Int]("totalFound")
      ).mapN(Category.apply)
    }
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Category): FreeApplicative[Codec.Field[A, *], Category] =
    (
      field("categoryId", f(_).id),
      field("categoryName", f(_).name),
      field("catalogName", f(_).catalogName),
      field("currentPage", f(_).currentPage),
      field("totalPages", f(_).totalPages),
      field("totalFoundItems", f(_).totalFoundItems)
    ).mapN(apply)
}
