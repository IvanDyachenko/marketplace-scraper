package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import tethys.JsonReader

@derive(loggable)
sealed trait SellerList

object SellerList {

  @derive(loggable, decoder, tethysReader)
  final case class Failure(error: String) extends SellerList

  @derive(loggable, decoder, tethysReader)
  final case class Success(items: List[MarketplaceSeller]) extends SellerList

  private val circeDecoders: Decoder[SellerList] =
    List[Decoder[SellerList]](Decoder[Failure].widen, Decoder[Success].widen).reduceLeft(_ or _)

  implicit def circeDecoder(component: Component.SellerList): Decoder[SellerList] = Decoder.instance[SellerList] { (c: HCursor) =>
    c.downField(component.stateId).as[SellerList](circeDecoders)
  }

  implicit def tethysJsonReader(component: Component.SellerList): JsonReader[SellerList] =
    JsonReader.builder
      .addField(
        component.stateId,
        JsonReader.builder
          .addField[Option[List[MarketplaceSeller]]]("items")
          .addField[Option[String]]("error")
          .buildReader {
            case (Some(items), _) => Success(items)
            case (_, None)        => Success(List.empty)
            case (_, Some(error)) => Failure(error)
          }
      )
      .buildReader(identity)
}
