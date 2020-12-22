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
  object VendorId extends TaggedType[Long] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val avroCodec: Codec[Type]      = lift
  }
  type VendorId = VendorId.Type

  implicit val circeDecoder: Decoder[Vendor] = deriveDecoder
  implicit val avroCodec: Codec[Vendor]      = Codec.derive[Vendor]
}
