package net.dalytics.models.ozon

import derevo.derive
import derevo.tethys.tethysReader
import io.circe.{Decoder, DecodingFailure, HCursor, Json}
import tethys.JsonReader
import tethys.derivation.semiauto._
import tethys.derivation.builder.ReaderBuilder
import supertagged.TaggedType

import net.dalytics.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedTethys, LiftedVulcanCodec}

final case class Catalog(
  page: Page,
  category: Category,
  categoryMenu: Option[CategoryMenu],
  searchResultsV2: Option[SearchResultsV2],
  soldOutResultsV2: Option[SoldOutResultsV2]
)

object Catalog {
  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedTethys with LiftedVulcanCodec
  type Name = Name.Type

  final case class Shared(page: Page, catalog: Shared.Catalog) {
    def category: Category = catalog.category
  }

  object Shared {
    @derive(tethysReader)
    private[Shared] final case class Catalog(category: Category)

    implicit val tethysJsonReader: JsonReader[Shared] = jsonReader[Shared] {
      describe {
        // format: off
        ReaderBuilder[Shared]
          .extract(_.page).from("catalog".as[Page])(identity)        
          .extract(_.catalog).from("catalog".as[Catalog])(identity)
        // format: on
      }
    }
  }

  implicit def tethysJsonReader(layout: Layout): JsonReader[Catalog] = {
    implicit val categoryMenuJsonReader: JsonReader[CategoryMenu] = {
      val component = layout.categoryMenu.getOrElse(Component.CategoryMenu(Component.Unknown.stateId))
      CategoryMenu.tethysJsonReader(component)
    }

    implicit val searchResultsV2JsonReader: JsonReader[SearchResultsV2] = {
      val component = layout.searchResultsV2.getOrElse(Component.SearchResultsV2(Component.Unknown.stateId))
      SearchResultsV2.tethysJsonReader(component)
    }

    implicit val soldOutResultsV2JsonReader: JsonReader[SoldOutResultsV2] = {
      val component = layout.soldOutResultsV2.getOrElse(Component.SoldOutResultsV2(Component.Unknown.stateId))
      SoldOutResultsV2.tethysJsonReader(component)
    }

    JsonReader.builder
      .addField[Shared]("shared")
      .addField[Option[CategoryMenu]]("categoryMenu")
      .addField[Option[SearchResultsV2]]("searchResultsV2")
      .addField[Option[SoldOutResultsV2]]("soldOutResultsV2")
      .buildReader { case (Shared(page, catalog), categoryMenu, searchResultsV2, soldOutResultsV2) =>
        apply(page, catalog.category, categoryMenu, searchResultsV2, soldOutResultsV2)
      }
  }

  implicit def circeDecoder(layout: Layout): Decoder[Catalog] = Decoder.instance[Catalog] { (c: HCursor) =>
    lazy val i = c.downField("shared").downField("catalog")

    for {
      catalogJson      <- i.as[Json]
      page             <- catalogJson.as[Page].left.map { failure =>
                            val message = s"${failure.message}. ${catalogJson.noSpacesSortKeys}"
                            DecodingFailure(message, failure.history)
                          }
      category         <- i.get[Category]("category").left.map { failure =>
                            val message = s"${failure.message}. ${catalogJson.noSpacesSortKeys}"
                            DecodingFailure(message, failure.history)
                          }
      categoryMenu     <- layout.categoryMenu.fold[Decoder.Result[Option[CategoryMenu]]](Right(None: Option[CategoryMenu])) { component =>
                            c.get[CategoryMenu]("categoryMenu")(CategoryMenu.circeDecoder(component)).map(Some(_))
                          }
      searchResultsV2  <- layout.searchResultsV2.fold[Decoder.Result[Option[SearchResultsV2]]](Right(None: Option[SearchResultsV2])) { component =>
                            c.get[SearchResultsV2]("searchResultsV2")(SearchResultsV2.circeDecoder(component)).map(Some(_))
                          }
      soldOutResultsV2 <- layout.soldOutResultsV2.fold[Decoder.Result[Option[SoldOutResultsV2]]](Right(None: Option[SoldOutResultsV2])) { component =>
                            c.get[SoldOutResultsV2]("soldOutResultsV2")(SoldOutResultsV2.circeDecoder(component)).map(Some(_))
                          }
    } yield Catalog(page, category, categoryMenu, searchResultsV2, soldOutResultsV2)
  }
}
