package marketplace.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Информация о производителе.
  *
  * @param id   Идентификатор производителя.
  * @param name Наименование производителя.
  */
@derive(loggable, decoder)
final case class Vendor(id: Vendor.VendorId, name: String)

object Vendor {

  /** Уникальный идентификатор производителя.
    */
  object VendorId extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type VendorId = VendorId.Type
}
