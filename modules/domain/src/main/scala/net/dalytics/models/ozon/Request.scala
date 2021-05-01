package net.dalytics.models.ozon

import cats.implicits._
import cats.Show
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic._
import io.circe.Encoder
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}
import enumeratum.{CatsEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Snakecase
import tofu.logging.LoggableEnum
import supertagged.TaggedType
import supertagged.postfix._

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@AvroNamespace("ozon.models")
@derive(loggable)
sealed trait Request {
  def host: String                                     = "api.ozon.ru"
  def path: String                                     = "/composer-api.bx/page/json/v1"
  def url: Request.Url
  def page: Option[Request.Page]                       = None
  def soldOutPage: Option[Request.SoldOutPage]         = None
  def layoutContainer: Option[Request.LayoutContainer] = None
  def layoutPageIndex: Option[Request.LayoutPageIndex] = None
  def searchFilters: List[SearchFilter]                = List.empty
  def searchFilterKey: Option[SearchFilter.Key]        = None
}

object Request {
  @derive(loggable)
  final case class GetSellerList(onPage: Request.Page) extends Request {
    val url                      = Url("/seller")
    override val page            = Some(onPage)
    override val layoutContainer = Some(LayoutContainer.Default)
    override val layoutPageIndex = page.map(_.self @@ LayoutPageIndex)
  }

  object GetSellerList {
    implicit val vulcanCodec: Codec[GetSellerList] = Codec.record[GetSellerList](name = "GetSellerList", namespace = "ozon.models") { field =>
      field("host", _.host) *> field("path", _.path) *> field("url", _.url) *>
        field("page", _.onPage).map(apply)
    }
  }

  @derive(loggable)
  final case class GetCategoryMenu(categoryId: Category.Id) extends Request {
    val url = Url(s"/modal/categoryMenu/category/${categoryId.show}/")
  }

  @derive(loggable)
  final case class GetCategorySearchFilterValues(categoryId: Category.Id, withSearchFilterKey: SearchFilter.Key) extends Request {
    override val path            = "/composer-api.bx/_action/getSearchFilterValues"
    override val url             = Url(s"/modal/filters/category/${categoryId.show}")
    override val searchFilterKey = Some(withSearchFilterKey)
  }

  @derive(loggable)
  final case class GetCategorySearchResultsV2(
    categoryId: Category.Id,
    onPage: Request.Page,
    withSearchFilters: List[SearchFilter]
  ) extends Request {
    val url                      = Url(s"/category/${categoryId.show}/")
    override val page            = Some(onPage)
    override val layoutContainer = Some(LayoutContainer.Default)
    override val layoutPageIndex = page.map(_.self @@ LayoutPageIndex)
    override val searchFilters   = withSearchFilters
  }

  object GetCategorySearchResultsV2 {
    implicit val vulcanCodec: Codec[GetCategorySearchResultsV2] =
      Codec.record[GetCategorySearchResultsV2](name = "GetCategorySearchResultsV2", namespace = "ozon.models") { field =>
        field("host", _.host) *> field("path", _.path) *> field("url", _.url) *>
          (field("categoryId", _.categoryId), field("page", _.onPage), field("searchFilters", _.withSearchFilters)).mapN(apply)
      }
  }

  @derive(loggable)
  final case class GetCategorySoldOutResultsV2(
    categoryId: Category.Id,
    onPage: Request.SoldOutPage,
    withSearchFilters: List[SearchFilter]
  ) extends Request {
    val url                      = Url(s"/category/${categoryId.show}/")
    override val page            = Some(0 @@ Page)
    override val soldOutPage     = Some(onPage)
    override val layoutContainer = Some(LayoutContainer.Default)
    override val layoutPageIndex = soldOutPage.map(_.self @@ LayoutPageIndex)
    override val searchFilters   = withSearchFilters
  }

  object GetCategorySoldOutResultsV2 {
    implicit val vulcanCodec: Codec[GetCategorySoldOutResultsV2] =
      Codec.record[GetCategorySoldOutResultsV2](name = "GetCategorySoldOutResultsV2", namespace = "ozon.models") { field =>
        field("host", _.host) *> field("path", _.path) *> field("url", _.url) *>
          (field("categoryId", _.categoryId), field("soldOutPage", _.onPage), field("searchFilter", _.withSearchFilters)).mapN(apply)
      }
  }

  @AvroNamespace("ozon.models.request")
  final case class Url(path: String) extends AnyVal

  object Url {
    implicit val queryParam = new QueryParam[Url] with QueryParamEncoder[Url] {
      val key                                   = QueryParameterKey("url")
      def encode(url: Url): QueryParameterValue = QueryParameterValue(url.show)
    }

    implicit val show: Show[Url]            = Show.show(_.path)
    implicit val circeEncoder: Encoder[Url] = Encoder.encodeString.contramap(_.path)
    implicit val vulcanCodec: Codec[Url]    = Codec.derive[Url]
  }

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
  case class LayoutContainer private (name: LayoutContainer.Name)

  object LayoutContainer {
    sealed trait Name extends EnumEntry with Snakecase with Product with Serializable
    object Name       extends Enum[Name] with CatsEnum[Name] with LoggableEnum[Name] with VulcanEnum[Name] {
      val values = findValues

      case object Default extends Name
    }

    object Default extends LayoutContainer(LayoutContainer.Name.Default)

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

  implicit val circeEncoder: Encoder[Request] = Encoder.forProduct2("url", "key")(request => (request.url, request.searchFilterKey))

  implicit val vulcanCodec: Codec[Request] = Codec.union[Request] { alt =>
    alt[GetSellerList] |+| alt[GetCategorySearchResultsV2] |+| alt[GetCategorySoldOutResultsV2]
  }
}
