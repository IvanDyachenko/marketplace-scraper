package marketplace.models.ozon

import cats.implicits._
import cats.free.{Cofree, FreeApplicative}
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Category(id: Category.Id, name: Category.Name, childrens: List[Category] = List.empty, catalogName: Option[Catalog.Name] = None) {
  val link: Url = Url(s"/category/${id.show}")
}

object Category {
  type Tree[F[_]] = Cofree[F, List[Category]]

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  object Path extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Path = Path.Type

  def apply(id: Id, name: Name, catalogName: Catalog.Name): Category = apply(id, name, catalogName = Some(catalogName))

  implicit val circeDecoder: Decoder[Category] = Decoder.instance[Category] { (c: HCursor) =>
    (
      c.get[Id]("id"),
      c.get[Name]("name"),
      c.get[Option[List[Category]]]("categories"),
      c.get[Option[Catalog.Name]]("catalogNam")
    ).mapN {
      case (id, name, Some(childrens), catalogName) => apply(id, name, childrens, catalogName)
      case (id, name, _, catalogName)               => apply(id, name, catalogName = catalogName)
    }
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Category): FreeApplicative[Codec.Field[A, *], Category] =
    (field("categoryId", f(_).id), field("categoryName", f(_).name), field("categoryCatalogName", f(_).catalogName)).mapN((id, name, catalogName) =>
      Category(id, name, catalogName = catalogName)
    )
}
