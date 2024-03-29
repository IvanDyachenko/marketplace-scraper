package net.dalytics.models.yandex.market

import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Lowercase

sealed trait Section extends EnumEntry with Lowercase with Product with Serializable

object Section extends Enum[Section] with CatsEnum[Section] with LoggableEnum[Section] {
  val values = findValues

  case object Medicine extends Section
}
