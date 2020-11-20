package marketplace.models.yandex.market

import enumeratum.{CatsEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Lowercase
import tofu.logging.LoggableEnum

sealed trait Section extends EnumEntry with Lowercase with Product with Serializable

object Section extends Enum[Section] with CatsEnum[Section] with LoggableEnum[Section] {

  case object Medicine extends Section

  val values = findValues
}
