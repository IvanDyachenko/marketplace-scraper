package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.Loggable
import tofu.logging.derivation.loggable

/** Информация о категории.
  *
  * @param id         Идентификатор категории.
  * @param name       Наименование категории.
  * @param fullName   Полное наименование категории.
  * @param childCount Количество дочерних категорий.
  */
@derive(loggable)
final case class Category(
  id: Category.CategoryId,
  name: Option[String] = None,
  fullName: Option[String] = None,
  childCount: Option[Int] = None
)

object Category {
  implicit val circeDecoder: Decoder[Category] = deriveDecoder

  /** Идентификатор категории.
    */
  object CategoryId extends TaggedType[Int] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = Loggable.intLoggable.contramap(identity)
    implicit val circeDecoder: Decoder[Type] = lift
  }
  type CategoryId = CategoryId.Type
}
