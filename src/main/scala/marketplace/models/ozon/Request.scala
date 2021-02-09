package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic.AvroNamespace
import supertagged.postfix._

import marketplace.models.ozon.Url

@derive(loggable)
@AvroNamespace("ozon.models")
sealed trait Request {
  def host: String = "api.ozon.ru"
  def path: String = "/composer-api.bx/page/json/v1"
  def url: Url
}

object Request {

  @derive(loggable)
  final case class GetCategoryMenu(categoryId: Category.Id) extends Request {
    val url = Url(s"/modal/categoryMenu/category/${categoryId.show}/")
  }

  @derive(loggable)
  final case class GetCategorySearchResultsV2 private (
    categoryId: Option[Category.Id],
    categoryName: Option[Category.Name],
    page: Url.Page,
    layoutContainer: Url.LayoutContainer,
    layoutPageIndex: Url.LayoutPageIndex
  ) extends Request {
    val url = Url(s"/category/${categoryId.show}/", Some(page), Some(layoutContainer), Some(layoutPageIndex))
  }

  object GetCategoryMenu {
    implicit val vulcanCodec: Codec[GetCategoryMenu] =
      Codec.record[GetCategoryMenu](name = "GetCategoryMenu", namespace = "ozon.models")(field =>
        field("host", _.host) *> field("path", _.path) *> field("url", _.url) *> field("categoryId", _.categoryId).map(apply)
      )
  }

  object GetCategorySearchResultsV2 {
    def apply(categoryId: Category.Id, page: Url.Page): GetCategorySearchResultsV2 =
      GetCategorySearchResultsV2(Some(categoryId), None, page, Url.LayoutContainer.Default, page.self @@ Url.LayoutPageIndex)

    implicit val vulcanCodec: Codec[GetCategorySearchResultsV2] =
      Codec.record[GetCategorySearchResultsV2](name = "GetCategorySearchResultsV2", namespace = "ozon.models") { field =>
        field("host", _.host) *> field("path", _.path) *> field("url", _.url) *>
          (
            field("categoryId", _.categoryId),
            field("categoryName", _.categoryName),
            field("page", _.page),
            field("layoutContainer", _.layoutContainer),
            field("layoutPageIndex", _.layoutPageIndex)
          ).mapN(apply)
      }
  }

  implicit val vulcanCodec: Codec[Request] = Codec.union[Request](alt => alt[GetCategoryMenu] |+| alt[GetCategorySearchResultsV2])
}
