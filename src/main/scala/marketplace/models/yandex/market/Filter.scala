package marketplace.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Uppercase
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

/** Параметры модели, по которым можно отфильтровать предложения.
  *
  * @param id   Идентификатор фильтра.
  * @param name Наименование фильтра.
  * @param type Тип фильтра.
  */
@derive(loggable, decoder)
final case class Filter(id: Filter.FilterId, name: String, `type`: Filter.Type)

object Filter {

  /** Идентификатор фильтра.
    */
  object FilterId extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce
  type FilterId = FilterId.Type

  /** Тип фильтра.
    */
  sealed abstract class Type extends EnumEntry with Uppercase with Product with Serializable
  object Type                extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] {
    val values = findValues

    case object Boolean extends Type // Логический тип.
    case object Number  extends Type // Числовой тип, задает диапазон допустимых значений.
    case object Enum    extends Type // Тип перечисление, задает список допустимых значений, множественный выбор.
    case object Color   extends Type // Фильтр по цвету, аналогичен [[Enum]].
    case object Size    extends Type // Фильтр по размеру, аналогичен [[Enum]]
    case object Radio   extends Type // Аналогичен [[Enum]], но допускает выбор только одного значения.
    case object Text    extends Type // Тип фильтра для фильтрации по поисковой фразе.
  }
}
