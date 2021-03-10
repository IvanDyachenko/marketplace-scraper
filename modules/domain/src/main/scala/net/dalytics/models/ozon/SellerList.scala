package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
sealed trait SellerList extends Result

object SellerList {

  @derive(loggable, decoder)
  final case class Success(items: List[MarketplaceSeller]) extends SellerList

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SellerList

  implicit val circeDecoder: Decoder[SellerList] = Decoder.instance[SellerList] { (c: HCursor) =>
    for {
      layout     <- c.get[Layout]("layout")
      sellerList <- layout.sellerList.fold[Decoder.Result[SellerList]](
                      Left(
                        DecodingFailure("'layout' object doesn't contain component which corresponds to 'sellerList'", c.history)
                      )
                    ) { component =>
                      val circeDecoder = List[Decoder[SellerList]](
                        Decoder[Failure].widen,
                        Decoder[Success].widen
                      ).reduceLeft(_ or _)

                      c.downField("cms").downField("sellerList").downField(component.stateId).as[SellerList](circeDecoder)
                    }
    } yield sellerList
  }
}
