package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}

@derive(loggable)
sealed trait SellerList

object SellerList {

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SellerList

  @derive(loggable, decoder)
  final case class Success(items: List[MarketplaceSeller]) extends SellerList

  implicit def circeDecoder(component: Component.SellerList): Decoder[SellerList] = Decoder.instance[SellerList] { (c: HCursor) =>
    val circeDecoders = List[Decoder[SellerList]](
      Decoder[Failure].widen,
      Decoder[Success].widen
    ).reduceLeft(_ or _)

    c.downField(component.stateId).as[SellerList](circeDecoders)
  }
}
