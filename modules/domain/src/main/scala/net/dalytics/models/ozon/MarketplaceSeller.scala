package net.dalytics.models.ozon

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable, decoder)
final case class MarketplaceSeller(id: MarketplaceSeller.Id, title: String, subtitle: String)

object MarketplaceSeller {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Id = Id.Type
}
