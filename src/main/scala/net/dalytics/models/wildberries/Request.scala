package net.dalytics.models.wildberries

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Lowercase
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

@derive(loggable)
sealed trait Request {
  def host: String
  def path: String
  def lang: Request.Lang     = Request.Lang.Ru
  def locale: Request.Locale = Request.Locale.Ru
}

object Request {

  @derive(loggable)
  final object GetCatalogMenu extends Request {
    val host: String = "wbxmenu-ru.wildberries.ru"
    val path: String = "/v2/api"
  }

  sealed abstract class Lang extends EnumEntry with Lowercase with Product with Serializable
  object Lang                extends Enum[Lang] with CatsEnum[Lang] with CirceEnum[Lang] with LoggableEnum[Lang] {
    val values = findValues

    case object Ru extends Lang

    implicit val queryParam = new QueryParam[Lang] with QueryParamEncoder[Lang] {
      val key                                      = QueryParameterKey("lang")
      def encode(value: Lang): QueryParameterValue = QueryParameterValue(value.show)
    }
  }

  sealed abstract class Locale extends EnumEntry with Lowercase with Product with Serializable
  object Locale                extends Enum[Locale] with CatsEnum[Locale] with CirceEnum[Locale] with LoggableEnum[Locale] {
    val values = findValues

    case object Ru extends Locale

    implicit val queryParam = new QueryParam[Locale] with QueryParamEncoder[Locale] {
      val key                                        = QueryParameterKey("locale")
      def encode(value: Locale): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
}
