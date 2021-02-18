package marketplace.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Информация о магазине.
  *
  * @param id Идентификатор магазина.
  */
@derive(loggable, decoder)
final case class Shop(id: Shop.ShopId)

object Shop {

  /** Уникальный идентификатор магазина.
    */
  object ShopId extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce
  type ShopId = ShopId.Type
}
