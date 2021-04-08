package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic.AvroNamespace
import supertagged.postfix._

import net.dalytics.models.ozon.Url

@derive(loggable)
@AvroNamespace("ozon.models")
sealed trait Request {
  def host: String = "api.ozon.ru"
  def path: String = "/composer-api.bx/page/json/v1"
  def url: Url
}

object Request {

  @derive(loggable)
  final case class GetSellerList(page: Url.Page) extends Request {
    private val layoutContainer: Url.LayoutContainer = Url.LayoutContainer.Default
    private val layoutPageIndex: Url.LayoutPageIndex = page.self @@ Url.LayoutPageIndex

    val url = Url(path = "/seller", layoutContainer = Some(layoutContainer), layoutPageIndex = Some(layoutPageIndex), page = Some(page))
  }

  object GetSellerList {
    implicit val vulcanCodec: Codec[GetSellerList] =
      Codec.record[GetSellerList](
        name = "GetSellerList",
        namespace = "ozon.models"
      )(field => field("host", _.host) *> field("path", _.path) *> field("page", _.page).map(apply))
  }

  @derive(loggable)
  final case class GetCategoryMenu(categoryId: Category.Id) extends Request {
    val url = Url(s"/modal/categoryMenu/category/${categoryId.show}/")
  }

  @derive(loggable)
  final case class GetCategorySearchResultsV2 private (
    categoryId: Category.Id,
    page: Url.Page,
    searchFilter: Option[SearchFilter] = None
  ) extends Request {
    private val layoutContainer: Url.LayoutContainer = Url.LayoutContainer.Default
    private val layoutPageIndex: Url.LayoutPageIndex = page.self @@ Url.LayoutPageIndex

    val url = Url(
      path = s"/category/${categoryId.show}/",
      layoutContainer = Some(layoutContainer),
      layoutPageIndex = Some(layoutPageIndex),
      page = Some(page),
      searchFilter = searchFilter
    )
  }

  object GetCategorySearchResultsV2 {
    implicit val vulcanCodec: Codec[GetCategorySearchResultsV2] =
      Codec.record[GetCategorySearchResultsV2](
        name = "GetCategorySearchResultsV2",
        namespace = "ozon.models"
      ) { field =>
        field("host", _.host) *> field("path", _.path) *>
          (field("categoryId", _.categoryId), field("page", _.page), field("searchFilter", _.searchFilter)).mapN(apply)
      }
  }

  @derive(loggable)
  final case class GetCategorySoldOutResultsV2 private (
    categoryId: Category.Id,
    page: Url.Page = 0 @@ Url.Page,
    soldOutPage: Url.SoldOutPage,
    searchFilter: Option[SearchFilter]
  ) extends Request {
    private val layoutContainer: Url.LayoutContainer = Url.LayoutContainer.Default
    private val layoutPageIndex: Url.LayoutPageIndex = (page.self + soldOutPage.self) @@ Url.LayoutPageIndex

    val url = Url(
      path = s"/category/${categoryId.show}/",
      layoutContainer = Some(layoutContainer),
      layoutPageIndex = Some(layoutPageIndex),
      page = Some(page),
      soldOutPage = Some(soldOutPage),
      searchFilter = searchFilter
    )
  }

  object GetCategorySoldOutResultsV2 {
    implicit val vulcanCodec: Codec[GetCategorySoldOutResultsV2] =
      Codec.record[GetCategorySoldOutResultsV2](
        name = "GetCategorySoldOutResultsV2",
        namespace = "ozon.models"
      ) { field =>
        field("host", _.host) *> field("path", _.path) *>
          (field("categoryId", _.categoryId), field("page", _.page), field("soldOutPage", _.soldOutPage), field("searchFilter", _.searchFilter))
            .mapN(apply)
      }
  }

  @derive(loggable)
  final case class GetCategorySearchFilterValues(categoryId: Category.Id, searchFilterKey: SearchFilter.Key) extends Request {
    override val path: String = "/composer-api.bx/_action/getSearchFilterValues"

    val url = Url(path = s"/modal/filters/category/${categoryId.show}", searchFilterKey = Some(searchFilterKey))
  }

  implicit val vulcanCodec: Codec[Request] = Codec.union[Request] { alt =>
    alt[GetSellerList] |+| alt[GetCategorySearchResultsV2] |+| alt[GetCategorySoldOutResultsV2]
  }
}
