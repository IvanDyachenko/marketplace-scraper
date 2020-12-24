package marketplace.models.yandex.market

import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

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
  object ShopId extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type ShopId = ShopId.Type

  implicit val circeDecoder: Decoder[Shop] = deriveDecoder
  implicit val avroCodec: Codec[Shop]      = Codec.derive[Shop]
}
