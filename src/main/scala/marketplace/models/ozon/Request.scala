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
    categoryId: Category.Id,
    categoryName: Option[Category.Name] = None,
    page: Url.Page
  ) extends Request {
    val layoutContainer: Url.LayoutContainer = Url.LayoutContainer.Default
    val layoutPageIndex: Url.LayoutPageIndex = page.self @@ Url.LayoutPageIndex

    val url = Url(s"/category/${categoryId.show}/", Some(page), Some(layoutContainer), Some(layoutPageIndex))
  }

  object GetCategoryMenu {
    implicit val vulcanCodec: Codec[GetCategoryMenu] =
      Codec.record[GetCategoryMenu](
        name = "GetCategoryMenu",
        namespace = "ozon.models"
      )(field => field("host", _.host) *> field("path", _.path) *> field("categoryId", _.categoryId).map(apply))
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

  implicit val vulcanCodec: Codec[Request] = Codec.union[Request](alt => alt[GetCategoryMenu] |+| alt[GetCategorySearchResultsV2])
}
