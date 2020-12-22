package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

/** Информация о магазине.
  *
  * @param id Идентификатор магазина.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class Shop(id: Shop.ShopId)

object Shop {

  /** Уникальный идентификатор магазина.
    */
  object ShopId extends TaggedType[Long] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val avroCodec: Codec[Type]      = lift
  }
  type ShopId = ShopId.Type

  implicit val circeDecoder: Decoder[Shop] = deriveDecoder
  implicit val avroCodec: Codec[Shop]      = Codec.derive[Shop]
}
