package net.dalytics.models.ozon

import cats.implicits._
import cats.free.{Cofree, FreeApplicative}
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Category(
  id: Category.Id,
  name: Category.Name,
  children: Map[Category.Id, Category] = Map.empty,
  catalogName: Option[Catalog.Name] = None
) {
  val isLeaf: Boolean = children.isEmpty

  def find(categoryId: Category.Id): Option[Category] = tree.collectFirst { case category if category.id == categoryId => category }

  private lazy val tree: Category.Tree[List] = Cofree.unfold[List, Category](this)(_.children.values.toList)
}

object Category {
  type Tree[F[_]] = Cofree[F, Category]

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Name = Name.Type

  object Path extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Path = Path.Type

  def apply(id: Id, name: Name, catalogName: Catalog.Name): Category = apply(id, name, catalogName = Some(catalogName))

  implicit final val childrenLoggable: Loggable[Map[Id, Category]] = Loggable.empty

  implicit final val circeDecoder: Decoder[Category] = Decoder.instance[Category] { (c: HCursor) =>
    (
      c.get[Id]("id"),
      c.get[Name]("name"),
      c.get[Option[List[Category]]]("categories"),
      c.get[Option[Catalog.Name]]("catalogName")
    ).mapN {
      case (id, name, None, catalogName)           => apply(id, name, catalogName = catalogName)
      case (id, name, Some(children), catalogName) => apply(id, name, children.groupMapReduce[Id, Category](_.id)(identity)((c, _) => c), catalogName)
    }
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Category): FreeApplicative[Codec.Field[A, *], Category] =
    (field("categoryId", f(_).id), field("categoryName", f(_).name), field("categoryCatalogName", f(_).catalogName)).mapN((id, name, catalogName) =>
      Category(id, name, catalogName = catalogName)
    )
}
