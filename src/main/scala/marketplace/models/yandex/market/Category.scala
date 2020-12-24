package marketplace.models.yandex.market

import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.derivation.loggable

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

/** Информация о категории.
  *
  * @param id         Идентификатор категории.
  * @param name       Наименование категории.
  * @param fullName   Полное наименование категории.
  * @param childCount Количество дочерних категорий.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
final case class Category(
  id: Category.CategoryId,
  name: Option[String] = None,
  fullName: Option[String] = None,
  childCount: Option[Int] = None
)

object Category {

  /** Идентификатор категории.
    */
  object CategoryId extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type CategoryId = CategoryId.Type

  implicit val circeDecoder: Decoder[Category] = deriveDecoder
  implicit val avroCodec: Codec[Category]      = Codec.derive[Category]
}
