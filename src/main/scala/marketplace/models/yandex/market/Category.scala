package marketplace.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Информация о категории.
  *
  * @param id         Идентификатор категории.
  * @param name       Наименование категории.
  * @param fullName   Полное наименование категории.
  * @param childCount Количество дочерних категорий.
  */
@derive(loggable, decoder)
final case class Category(
  id: Category.CategoryId,
  name: Option[String] = None,
  fullName: Option[String] = None,
  childCount: Option[Int] = None
)

object Category {

  /** Идентификатор категории.
    */
  object CategoryId extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce {}
  type CategoryId = CategoryId.Type
}
