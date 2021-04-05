package net.dalytics.models.ozon

import io.circe.{Decoder, DecodingFailure, HCursor, Json}
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

final case class Catalog private (
  page: Page,
  category: Category,
  categoryMenu: Option[CategoryMenu],
  searchResultsV2: Option[SearchResultsV2]
)

object Catalog {
  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec
  type Name = Name.Type

  implicit def circeDecoder(layout: Layout): Decoder[Catalog] = Decoder.instance[Catalog] { (c: HCursor) =>
    lazy val i = c.downField("shared").downField("catalog")

    for {
      catalogJson     <- i.as[Json]
      page            <- catalogJson.as[Page].left.map { failure =>
                           val message = s"${failure.message}. ${catalogJson.noSpacesSortKeys}"
                           DecodingFailure(message, failure.history)
                         }
      category        <- i.get[Category]("category").left.map { failure =>
                           val message = s"${failure.message}. ${catalogJson.noSpacesSortKeys}"
                           DecodingFailure(message, failure.history)
                         }
      categoryMenu    <- layout.categoryMenu.fold[Decoder.Result[Option[CategoryMenu]]](Right(None: Option[CategoryMenu])) { component =>
                           c.get[CategoryMenu]("categoryMenu")(CategoryMenu.circeDecoder(component)).map(Some(_))
                         }
      searchResultsV2 <- layout.searchResultsV2.fold[Decoder.Result[Option[SearchResultsV2]]](Right(None: Option[SearchResultsV2])) { component =>
                           c.get[SearchResultsV2]("searchResultsV2")(SearchResultsV2.circeDecoder(component)).map(Some(_))
                         }
    } yield Catalog(page, category, categoryMenu, searchResultsV2)
  }
}
