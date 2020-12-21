package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import supertagged.lift.LiftF
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

/** Предложение.
  *
  * @param id            Идентификатор предложения.
  * @param wareMd5       MD5 хеш-код предложения.
  * @param name          Название предложения.
  * @param sku
  * @param skuType
  * @param model         Идентификатор модели.
  * @param vendor        Информация о производителе.
  * @param warranty      Признак наличия гарантии производителя.
  * @param recommended   Признак наличия рекомендации производителя.
  * @param shop          Информация о магазине, который разместил предложение.
  * @param price         Информация о цене.
  * @param promocode     Признак, что товар можно купить с промокодом.
  * @param activeFilters Параметры модели, по которым можно отфильтровать предложения.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class Offer(
  id: Offer.OfferId,
  wareMd5: Offer.MD5,
  name: String,
  sku: String,
  skuType: String,
  model: Model.ModelId,
  vendor: Vendor,
  warranty: Boolean,
  recommended: Boolean,
  shop: Shop,
  price: OfferPrice,
  promocode: Boolean,
  activeFilters: List[Filter]
)

object Offer {

  /** Идентификатор предложения.
    */
  object OfferId extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = LiftF[Decoder].lift[Raw, Tag](Decoder[Raw].or(Decoder[Raw].at("id")))
    implicit val avroCodec: Codec[Type]      = lift
  }
  type OfferId = OfferId.Type

  /** MD5 хеш-код предложения.
    */
  object MD5 extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val avroCodec: Codec[Type]      = lift
  }
  type MD5 = MD5.Type

  implicit val circeDecoder: Decoder[Offer] = deriveDecoder
  implicit val avroCodec: Codec[Offer]      = Codec.derive[Offer]
}

/** Информация о цене.
  *
  * @param value    Значение цены.
  * @param base     Базовая цена.
  * @param discount Скидка.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class OfferPrice(value: String, base: Option[String], discount: Option[String])

object OfferPrice {
  implicit val circeDecoder: Decoder[OfferPrice] = deriveDecoder
  implicit val avroCodec: Codec[OfferPrice]      = Codec.derive[OfferPrice]
}
