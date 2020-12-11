package marketplace.models.yandex.market

import cats.Show
import supertagged.TaggedType
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Uppercase
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.{Loggable, LoggableEnum}
import tofu.logging.derivation.loggable

/** Параметры модели, по которым можно отфильтровать предложения.
  *
  * @param id   Идентификатор фильтра.
  * @param name Наименование фильтра.
  * @param type Тип фильтра.
  */
@derive(loggable)
final case class Filter(id: Filter.FilterId, name: String, `type`: Filter.Type)

object Filter {
  implicit val circeDecoder: Decoder[Filter] = deriveDecoder

  /** Идентификатор фильтра.
    */
  object FilterId extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
  }
  type FilterId = FilterId.Type

  /** Тип фильтра.
    */
  sealed abstract class Type extends EnumEntry with Uppercase with Product with Serializable

  object Type extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] {

    case object Boolean extends Type // Логический тип.
    case object Number  extends Type // Числовой тип, задает диапазон допустимых значений.
    case object Enum    extends Type // Тип перечисление, задает список допустимых значений, множественный выбор.
    case object Color   extends Type // Фильтр по цвету, аналогичен [[Enum]].
    case object Size    extends Type // Фильтр по размеру, аналогичен [[Enum]]
    case object Radio   extends Type // Аналогичен [[Enum]], но допускает выбор только одного значения.
    case object Text    extends Type // Тип фильтра для фильтрации по поисковой фразе.

    val values = findValues
  }
}
