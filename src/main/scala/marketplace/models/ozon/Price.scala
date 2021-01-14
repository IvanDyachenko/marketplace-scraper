package marketplace.models.ozon

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable, decoder)
final case class Price(price: Int, finalPrice: Int, discount: Price.Percent)

object Price {
  object Percent extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Percent = Percent.Type
}
