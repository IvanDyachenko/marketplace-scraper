package net.dalytics.models.ozon

import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase

sealed trait SearchFilterKey extends EnumEntry with LowerCamelcase with Product with Serializable

object SearchFilterKey
    extends Enum[SearchFilterKey]
    with CatsEnum[SearchFilterKey]
    with CirceEnum[SearchFilterKey]
    with LoggableEnum[SearchFilterKey] {
  val values = findValues

  case object Brand extends SearchFilterKey
}
