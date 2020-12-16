package marketplace.models.yandex.market

import enumeratum.{CatsEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Lowercase
import tofu.logging.LoggableEnum
import vulcan.AvroNamespace

@AvroNamespace("yandex.market.models")
sealed trait Section extends EnumEntry with Lowercase with Product with Serializable

object Section extends Enum[Section] with CatsEnum[Section] with LoggableEnum[Section] with VulcanEnum[Section] {

  case object Medicine extends Section

  val values = findValues
}
