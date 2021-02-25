package marketplace.models.wildberries

import cats.implicits._
import cats.free.Cofree
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

@derive(loggable)
final case class Catalog(
  id: Catalog.Id,
  name: Catalog.Name,
  shardKey: Option[Catalog.ShardKey],
  url: Url,
  filters: Option[String],
  children: Map[Catalog.Id, Catalog] = Map.empty
) {
  val isLeaf: Boolean       = children.isEmpty
  def leaves: List[Catalog] = filter(_.isLeaf)

  def find(catalogId: Catalog.Id): Option[Catalog] = tree.collectFirst { case catalog if catalogId == catalog.id => catalog }
  def filter(p: Catalog => Boolean): List[Catalog] = tree.filter_(p)

  private lazy val tree: Catalog.Tree[List] = Cofree.unfold[List, Catalog](this)(_.children.values.toList)
}

object Catalog {
  type Tree[F[_]] = Cofree[F, Catalog]

  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type Name = Name.Type

  object ShardKey extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type ShardKey = ShardKey.Type

  def apply(id: Id, name: Name, shardKey: Option[ShardKey], url: Url, filters: Option[String], children: List[Catalog]): Catalog =
    apply(id, name, shardKey, url, filters, children.groupMapReduce[Id, Catalog](_.id)(identity)((c, _) => c))

  implicit final val childrenLoggable: Loggable[Map[Id, Catalog]] = Loggable.empty

  implicit final val circeDecoder: Decoder[Catalog] = Decoder.instance[Catalog] { (c: HCursor) =>
    (
      c.get[Id]("id"),
      c.get[Name]("name"),
      c.get[Option[ShardKey]]("shardKey"),
      c.as[Url],
      c.get[Option[String]]("filters"),
      c.getOrElse[List[Catalog]]("childNodes")(List.empty)
    ).mapN(apply)
  }
}
