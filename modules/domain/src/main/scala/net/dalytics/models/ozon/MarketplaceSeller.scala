package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import vulcan.Codec
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable, decoder)
final case class MarketplaceSeller(id: MarketplaceSeller.Id, title: String, subtitle: String)

object MarketplaceSeller {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Id = Id.Type

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(
    f: A => MarketplaceSeller
  ): FreeApplicative[Codec.Field[A, *], MarketplaceSeller] =
    (field("sellerId", f(_).id), field("sellerTitle", f(_).title), field("sellerSubtitle", f(_).subtitle)).mapN(apply)
}
