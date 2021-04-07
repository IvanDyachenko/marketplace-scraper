package net.dalytics.models.ozon

import io.circe.{Decoder, HCursor}

final case class Cms(
  sellerList: Option[SellerList]
)

object Cms {
  implicit def circeDecoder(layout: Layout): Decoder[Cms] = Decoder.instance[Cms] { (c: HCursor) =>
    for {
      sellerList <- layout.sellerList.fold[Decoder.Result[Option[SellerList]]](Right(None: Option[SellerList])) { component =>
                      c.get[SellerList]("sellerList")(SellerList.circeDecoder(component)).map(Some(_))
                    }
    } yield Cms(sellerList)
  }
}
