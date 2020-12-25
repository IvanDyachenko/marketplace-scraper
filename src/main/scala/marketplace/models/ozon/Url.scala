package marketplace.models.ozon

import cats.implicits._
import supertagged.TaggedType
import enumeratum.{CatsEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Snakecase
import derevo.derive
import tofu.logging.LoggableEnum
import tofu.logging.derivation.loggable
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue, Uri}

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
@AvroNamespace("ozon.models")
case class Url(
  path: String,
  page: Option[Url.Page],
  layoutContainer: Option[Url.LayoutContainer],
  layoutPageIndex: Option[Url.LayoutPageIndex]
)

object Url {
  object Page extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("page")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type Page = Page.Type

  @derive(loggable)
  @AvroNamespace("ozon.models.url")
  case class LayoutContainer(name: LayoutContainer.Name)

  object LayoutContainer {
    @AvroNamespace("ozon.models.url")
    sealed trait Name extends EnumEntry with Snakecase with Product with Serializable

    object Name extends Enum[Name] with CatsEnum[Name] with LoggableEnum[Name] with VulcanEnum[Name] {
      case object Default extends Name

      val values = findValues
    }

    implicit val queryParam = new QueryParam[LayoutContainer] with QueryParamEncoder[LayoutContainer] {
      val key                                                 = QueryParameterKey("layout_container")
      def encode(value: LayoutContainer): QueryParameterValue = QueryParameterValue(value.name.show)
    }

    implicit val avroCodec: Codec[LayoutContainer] = Codec.derive[LayoutContainer]
  }

  object LayoutPageIndex extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("layout_page_index")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type LayoutPageIndex = LayoutPageIndex.Type

  implicit val queryParam = new QueryParam[Url] with QueryParamEncoder[Url] {
    val key                                   = QueryParameterKey("url")
    def encode(url: Url): QueryParameterValue = QueryParameterValue(
      Uri(path = url.path).+??(url.page).+??(url.layoutContainer).+??(url.layoutPageIndex).show
    )
  }

  implicit val vulcanCodec: Codec[Url] = Codec.derive[Url]
}
