package net.dalytics.models.ozon

import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

object Catalog {
  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Name = Name.Type
}
