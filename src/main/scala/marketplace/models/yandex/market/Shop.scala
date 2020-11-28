package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

/** Информация о магазине.
  *
  * @param id Идентификатор магазина.
  */
@derive(loggable)
final case class Shop(id: Shop.ShopId)

object Shop {
  implicit val circeDecoder: Decoder[Shop] = deriveDecoder

  /** Уникальный идентификатор магазина.
    */
  object ShopId extends TaggedType[Long] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = Loggable.longLoggable.contramap(identity)
    implicit val circeDecoder: Decoder[Type] = lift
  }
  type ShopId = ShopId.Type
}
