package marketplace.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec

import marketplace.models.ozon.Url.{LayoutContainer, LayoutPageIndex, Page}

@derive(loggable)
sealed trait Request {
  def host: String = "api.ozon.ru"
  def path: String = "/composer-api.bx/page/json/v1"
  def url: Url
}

@derive(loggable)
final case class GetCategorySearchResultsV2(
  categoryName: Category.Name,
  page: Page,
  layoutContainer: LayoutContainer,
  layoutPageIndex: LayoutPageIndex
) extends Request {
  val url = Url(s"/category/${categoryName.show}/", Some(page), Some(layoutContainer), Some(layoutPageIndex))
}

object GetCategorySearchResultsV2 {
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
