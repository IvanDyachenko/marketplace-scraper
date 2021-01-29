package marketplace.models.ozon

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable, decoder)
case class Category(id: Category.Id, name: Category.Name, catalogName: Catalog.Name)

object Category {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Id = Id.Type

  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  object Path extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Path = Path.Type
}
