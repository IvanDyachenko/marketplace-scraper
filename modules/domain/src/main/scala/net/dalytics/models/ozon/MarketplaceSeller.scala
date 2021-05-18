package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import vulcan.Codec
import tethys.JsonReader
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

@derive(loggable, decoder)
final case class MarketplaceSeller(id: MarketplaceSeller.Id, title: String, subtitle: String)

object MarketplaceSeller {
  object Id extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Id = Id.Type

  implicit val jsonReader: JsonReader[MarketplaceSeller] = JsonReader.builder
    .addField[MarketplaceSeller.Id]("id")
    .addField[String]("title")
    .addField[Option[String]]("subtitle")
    .buildReader((id, title, subtitleOpt) => apply(id, title, subtitleOpt.getOrElse(title)))

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(
    f: A => MarketplaceSeller
  ): FreeApplicative[Codec.Field[A, *], MarketplaceSeller] =
    (field("sellerId", f(_).id), field("sellerTitle", f(_).title), field("sellerSubtitle", f(_).subtitle)).mapN(apply)
}
