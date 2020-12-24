package marketplace.models.yandex.market

import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

/** Информация о производителе.
  *
  * @param id   Идентификатор производителя.
  * @param name Наименование производителя.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class Vendor(id: Vendor.VendorId, name: String)

object Vendor {

  /** Уникальный идентификатор производителя.
    */
  object VendorId extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type VendorId = VendorId.Type

  implicit val circeDecoder: Decoder[Vendor] = deriveDecoder
  implicit val avroCodec: Codec[Vendor]      = Codec.derive[Vendor]
}
