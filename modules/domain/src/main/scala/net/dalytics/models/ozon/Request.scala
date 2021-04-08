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

    val url = Url("/seller", Some(page), None, Some(layoutContainer), Some(layoutPageIndex))
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

  object GetCategoryMenu {
    implicit val vulcanCodec: Codec[GetCategoryMenu] =
      Codec.record[GetCategoryMenu](
        name = "GetCategoryMenu",
        namespace = "ozon.models"
      )(field => field("host", _.host) *> field("path", _.path) *> field("categoryId", _.categoryId).map(apply))
  }

  @derive(loggable)
  final case class GetCategorySearchResultsV2 private (
    categoryId: Category.Id,
    categoryName: Option[Category.Name] = None,
    page: Url.Page
  ) extends Request {
    private val layoutContainer: Url.LayoutContainer = Url.LayoutContainer.Default
    private val layoutPageIndex: Url.LayoutPageIndex = page.self @@ Url.LayoutPageIndex

    val url = Url(s"/category/${categoryId.show}/", Some(page), None, Some(layoutContainer), Some(layoutPageIndex))
  }

  object GetCategorySearchResultsV2 {
    def apply(id: Category.Id, name: Category.Name, page: Url.Page): GetCategorySearchResultsV2 = GetCategorySearchResultsV2(id, Some(name), page)

    implicit val vulcanCodec: Codec[GetCategorySearchResultsV2] =
      Codec.record[GetCategorySearchResultsV2](
        name = "GetCategorySearchResultsV2",
        namespace = "ozon.models"
      ) { field =>
        field("host", _.host) *> field("path", _.path) *>
          (field("categoryId", _.categoryId), field("categoryName", _.categoryName), field("page", _.page)).mapN(apply)
      }
  }

  @derive(loggable)
  final case class GetCategorySoldOutResultsV2 private (
    categoryId: Category.Id,
    categoryName: Option[Category.Name] = None,
    page: Url.Page = 0 @@ Url.Page,
    soldOutPage: Url.SoldOutPage
  ) extends Request {
    private val layoutContainer: Url.LayoutContainer = Url.LayoutContainer.Default
    private val layoutPageIndex: Url.LayoutPageIndex = (page.self + soldOutPage.self) @@ Url.LayoutPageIndex

    val url = Url(s"/category/${categoryId.show}/", Some(page), Some(soldOutPage), Some(layoutContainer), Some(layoutPageIndex))
  }

  object GetCategorySoldOutResultsV2 {
    def apply(id: Category.Id, name: Category.Name, soldOutPage: Url.SoldOutPage): GetCategorySoldOutResultsV2 =
      GetCategorySoldOutResultsV2(id, Some(name), soldOutPage = soldOutPage)

    def apply(id: Category.Id, name: Category.Name, page: Url.Page, soldOutPage: Url.SoldOutPage): GetCategorySoldOutResultsV2 =
      GetCategorySoldOutResultsV2(id, Some(name), page, soldOutPage)

    implicit val vulcanCodec: Codec[GetCategorySoldOutResultsV2] =
      Codec.record[GetCategorySoldOutResultsV2](
        name = "GetCategorySoldOutResultsV2",
        namespace = "ozon.models"
      ) { field =>
        field("host", _.host) *> field("path", _.path) *>
          (field("categoryId", _.categoryId), field("categoryName", _.categoryName), field("page", _.page), field("soldOutPage", _.soldOutPage))
            .mapN(apply)
      }
  }

  @derive(loggable)
  sealed trait GetCategorySearchFilterValues extends Request {
    override val path: String = "/composer-api.bx/_action/getSearchFilterValues"
    def categoryId: Category.Id
  }

  @derive(loggable)
  final case class GetCategorySearchFilterBrands(categoryId: Category.Id) extends GetCategorySearchFilterValues {
    override val url: Url = Url(s"/modal/filters/category/${categoryId.show}", searchFilterKey = Some(SearchFilter.Key.Brand))
  }

  implicit val vulcanCodec: Codec[Request] = Codec.union[Request] { alt =>
    alt[GetSellerList] |+| alt[GetCategoryMenu] |+| alt[GetCategorySearchResultsV2] |+| alt[GetCategorySoldOutResultsV2]
  }
}
