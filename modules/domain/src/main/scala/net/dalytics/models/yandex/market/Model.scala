package net.dalytics.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.Decoder
import supertagged.TaggedType
import supertagged.lift.LiftF

import net.dalytics.models.{LiftedCats, LiftedLoggable}

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
@derive(loggable, decoder)
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
  object ModelId extends TaggedType[Long] with LiftedCats with LiftedLoggable {
    implicit val circeDecoder: Decoder[Type] = LiftF[Decoder].lift[Raw, Tag](Decoder[Raw].or(Decoder[Raw].at("id")))
  }
  type ModelId = ModelId.Type
}

@derive(loggable, decoder)
final case class ModelPrice(min: String, avg: String, max: String, discount: Option[String], base: Option[String])
