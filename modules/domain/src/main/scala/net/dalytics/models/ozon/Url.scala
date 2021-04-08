package net.dalytics.models.ozon

import cats.implicits._
import cats.Show
import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.Encoder
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue, Uri}
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Snakecase
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
case class Url(
  path: String,
  page: Option[Url.Page] = None,
  soldOutPage: Option[Url.SoldOutPage] = None,
  searchFilter: Option[SearchFilter] = None,
  searchFilterKey: Option[SearchFilter.Key] = None,
  layoutContainer: Option[Url.LayoutContainer] = None,
  layoutPageIndex: Option[Url.LayoutPageIndex] = None
)

object Url {
  object Page extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("page")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Page = Page.Type

  object SoldOutPage extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("sold_out_page")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type SoldOutPage = SoldOutPage.Type

  @derive(loggable)
  case class LayoutContainer(name: LayoutContainer.Name)

  object LayoutContainer {
    object Default extends LayoutContainer(LayoutContainer.Name.Default)

    sealed trait Name extends EnumEntry with Snakecase with Product with Serializable
    object Name       extends Enum[Name] with CatsEnum[Name] with LoggableEnum[Name] with VulcanEnum[Name] {
      val values = findValues

      case object Default extends Name
    }

    implicit val queryParam = new QueryParam[LayoutContainer] with QueryParamEncoder[LayoutContainer] {
      val key                                                 = QueryParameterKey("layout_container")
      def encode(value: LayoutContainer): QueryParameterValue = QueryParameterValue(value.name.show)
    }
  }

  object LayoutPageIndex extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("layout_page_index")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type LayoutPageIndex = LayoutPageIndex.Type

  implicit val show: Show[Url] = Show[Uri].contramap { url =>
    val uri = Uri(path = url.path).+??(url.layoutContainer).+??(url.layoutPageIndex).+??(url.page).+??(url.soldOutPage)
    url.searchFilter.fold(uri)(sf => uri.+?(sf.key, sf))
  }

  implicit val queryParam = new QueryParam[Url] with QueryParamEncoder[Url] {
    val key                                   = QueryParameterKey("url")
    def encode(url: Url): QueryParameterValue = QueryParameterValue(url.show)
  }

  implicit val circeEncoder: Encoder[Url] = Encoder.forProduct2("url", "key")(url => (url.show, url.searchFilterKey))
}
