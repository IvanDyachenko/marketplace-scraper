package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.{AvroNamespace, Codec}
import supertagged.postfix._

import marketplace.models.ozon.Url.{LayoutContainer, LayoutPageIndex, Page}

@derive(loggable)
@AvroNamespace("ozon.models")
sealed trait Request {
  def host: String = "api.ozon.ru"
  def path: String = "/composer-api.bx/page/json/v1"
  def url: Url
}

@derive(loggable)
final case class GetCategorySearchResultsV2(
  categoryName: Category.Name,
  page: Page,
  layoutContainer: LayoutContainer = LayoutContainer.Default,
  layoutPageIndex: LayoutPageIndex
) extends Request {
  val url = Url(s"/category/${categoryName.show}/", Some(page), Some(layoutContainer), Some(layoutPageIndex))
}

object GetCategorySearchResultsV2 {
  def apply(categoryName: Category.Name, page: Page): GetCategorySearchResultsV2 =
    GetCategorySearchResultsV2(categoryName, page, LayoutContainer.Default, page.self @@ LayoutPageIndex)

  implicit val vulcanCodec: Codec[GetCategorySearchResultsV2] =
    Codec.record[GetCategorySearchResultsV2](
      name = "GetCategorySearchResultsV2",
      namespace = "ozon.models"
    ) { field =>
      field("host", _.host) *> field("path", _.path) *> field("url", _.url) *>
        (
          field("categoryName", _.categoryName),
          field("page", _.page),
          field("layoutContainer", _.layoutContainer),
          field("layoutPageIndex", _.layoutPageIndex)
        ).mapN(apply)
    }
}

object Request {
  implicit val vulcanCodec: Codec[Request] = Codec.union[Request](alt => alt[GetCategorySearchResultsV2])
}
