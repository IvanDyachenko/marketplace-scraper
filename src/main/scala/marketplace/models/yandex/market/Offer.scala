package marketplace.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.Decoder
import supertagged.TaggedType
import supertagged.lift.LiftF

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

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
@derive(loggable, decoder)
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
  object OfferId extends TaggedType[String] with LiftedCats with LiftedLoggable {
    implicit val circeDecoder: Decoder[Type] = LiftF[Decoder].lift[Raw, Tag](Decoder[Raw].or(Decoder[Raw].at("id")))
  }
  type OfferId = OfferId.Type

  /** MD5 хеш-код предложения.
    */
  object MD5 extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type MD5 = MD5.Type
}

/** Информация о цене.
  *
  * @param value    Значение цены.
  * @param base     Базовая цена.
  * @param discount Скидка.
  */
@derive(loggable, decoder)
final case class OfferPrice(value: String, base: Option[String], discount: Option[String])
