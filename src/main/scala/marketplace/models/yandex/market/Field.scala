package marketplace.models.yandex.market

import enumeratum.{CatsEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.UpperSnakecase
import tofu.logging.LoggableEnum
import vulcan.AvroNamespace

/** Свойства, которые необходимо показать в выходных данных.
  */
@AvroNamespace("yandex.market.models")
sealed trait Field extends EnumEntry with UpperSnakecase with Product with Serializable

object Field extends Enum[Field] with CatsEnum[Field] with LoggableEnum[Field] with VulcanEnum[Field] {

  case object ModelVendor         extends Field // Информация о производителе.
  case object ModelCategory       extends Field // Информация о категории, к которой относится модель.
  case object ModelPrice          extends Field // Информация о ценах на модель.
  case object ModelDiscounts      extends Field // Информация о скидках на модель.
  case object ModelRating         extends Field // Информация о рейтинге и оценках модели.
  case object ModelMedia          extends Field // Информация об отзывах и обзорах на модель.
  case object ModelReasonsToBuy   extends Field
  case object ModelSpecification  extends Field // Характеристики модели.
  case object ModelFilterColor    extends Field // Список фильтров по цвету, доступных для отбора модификаций модели.
  case object ModelPhoto          extends Field // Изображение модели, используемое как основное.
  case object ModelPhotos         extends Field // Все доступные изображения модели.
  case object ModelOffers         extends Field // Информация о товарных предложениях в указанном регионе.
  case object ModelDefaultOffer   extends Field // Информация о товарном предложении по умолчанию в указанном регионе.
  case object OfferVendor         extends Field // Информация о поставщике.
  case object OfferCategory       extends Field // Информация о категории предложения.
  case object OfferShop           extends Field // Магазин от которого поступило предложение.
  case object OfferDelivery       extends Field // Информация о доставке.
  case object OfferOutlet         extends Field // Информация о точке выдачи производителя.
  case object OfferDiscount       extends Field // Скидка.
  case object OfferBundleSettings extends Field

  /** OFFER_ALL =
    *   OFFER_LINK,
    *   OFFER_PHOTO,
    *   OFFER_OFFERS_LINK,
    *   OFFER_CATEGORY,
    *   OFFER_VENDOR,
    *   OFFER_SUPPLIER,
    *   OFFER_SHOP,
    *   OFFER_DISCOUNT,
    *   OFFER_OUTLET,
    *   OFFER_DELIVERY,
    *   OFFER_ACTIVE_FILTERS,
    *   OFFER_BUNDLE_SETTINGS,
    */
  case object OfferAll extends Field

  /** SHOP_ALL =
    *   SHOP_RATING,
    *   SHOP_ORGANIZATION
    */
  case object ShopAll extends Field

  val values = findValues
}
