package net.dalytics.models.ozon

import cats.implicits._
import cats.free.{Cofree, FreeApplicative}
import derevo.derive
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

@derive(loggable, tethysReader)
final case class Category(
  id: Category.Id,
  name: Category.Name,
  categories: Option[List[Category]] = None,
  catalogName: Option[Catalog.Name] = None
) {
  val isLeaf: Boolean                      = categories.fold(true)(_.isEmpty)
  val children: Map[Category.Id, Category] = categories.getOrElse(List.empty).groupMapReduce[Category.Id, Category](_.id)(identity)((c, _) => c)

  def find(categoryId: Category.Id): Option[Category] = tree.collectFirst { case category if category.id == categoryId => category }

  private lazy val tree: Category.Tree[List] = Cofree.unfold[List, Category](this)(_.categories.getOrElse(List.empty))
}

object Category {
  def apply(id: Category.Id, name: Category.Name, catalogName: Catalog.Name): Category = Category(id, name, catalogName = Some(catalogName))

  type Tree[F[_]] = Cofree[F, Category]

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Name = Name.Type

  object Path extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Path = Path.Type

  implicit val circeDecoder: Decoder[Category] = Decoder.instance[Category] { (c: HCursor) =>
    (
      c.get[Id]("id"),
      c.get[Name]("name"),
      c.get[Option[List[Category]]]("categories"),
      c.get[Option[Catalog.Name]]("catalogName")
    ).mapN {
      case (id, name, None, catalogName)       => apply(id, name, catalogName = catalogName)
      case (id, name, categories, catalogName) => apply(id, name, categories, catalogName)
    }
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Category): FreeApplicative[Codec.Field[A, *], Category] =
    (
      field("categoryId", f(_).id),
      field("categoryName", f(_).name),
      field("categoryCatalogName", f(_).catalogName)
    ).mapN((id, name, catalogName) => Category(id, name, catalogName = catalogName))
}
