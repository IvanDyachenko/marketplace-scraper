package marketplace.models.yandex.market

import supertagged.TaggedType
import supertagged.lift.LiftF
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{LiftedCats, LiftedLoggable, LiftedVulcanCodec}

/** Информация о модели.
  *
  * @param id           Идентификатор модели.
  * @param link         Ссылка на карточку модели на большом маркете.
  * @param name         Наименование модели.
  * @param price        Информация о цене модели в основной валюте запроса.
  * @param category     Информация о категории, к которой относится модель.
  * @param parent       Информация о групповой модели к которой относится модификация.
  * @param rating       Информация о рейтинге модели.
  * @param reviewCount  Кол-во статей/обзоров на модель.
  * @param opinionCount Кол-во отзывов на модель.
  * @param offer        Дефолтное товарное предложение на модель.
  * @param offerCount   Кол-во товарных предложений модели в регионе запроса.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class Model(
  id: Model.ModelId,
  link: String,
  name: String,
  price: ModelPrice,
  category: Category,
  parent: Model.ModelId,
  rating: Rating,
  reviewCount: Int,
  opinionCount: Int,
  offer: Offer,
  offerCount: Int
)

object Model {

  /** Идентификатор модели.
    */
  object ModelId extends TaggedType[Long] with LiftedCats with LiftedLoggable with LiftedVulcanCodec {
    implicit val circeDecoder: Decoder[Type] = LiftF[Decoder].lift[Raw, Tag](Decoder[Raw].or(Decoder[Raw].at("id")))
  }
  type ModelId = ModelId.Type

  implicit val circeDecoder: Decoder[Model] = deriveDecoder
  implicit val avroCodec: Codec[Model]      = Codec.derive[Model]
}

@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class ModelPrice(min: String, avg: String, max: String, discount: Option[String], base: Option[String])

object ModelPrice {
  implicit val circeDecoder: Decoder[ModelPrice] = deriveDecoder
  implicit val avroCodec: Codec[ModelPrice]      = Codec.derive[ModelPrice]
}
