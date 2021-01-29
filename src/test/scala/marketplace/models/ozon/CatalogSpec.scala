package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

import supertagged.postfix._

class CatalogSpec extends AnyFlatSpec with Matchers {

  it should "decode Catalog with SearchResultsV2.Failure from a valid JSON" in {
    val catalogRawJson =
      """
        |{
        |  "shared" : {
        |    "catalog" : {
        |      "totalPages" : 70,
        |      "context" : "category",
        |      "currentUrl" : "/category/dlya-gryzunov-12429/?page=131",
        |      "categoryPredicted" : false,
        |      "correctedText" : "",
        |      "totalFound" : 2487,
        |      "currentText" : "",
        |      "category" : {
        |        "catalogName" : "",
        |        "imageUrls" : {
        |          "catalog_logo" : "https://cdn1.ozone.ru/multimedia/1033937063.jpg"
        |        },
        |        "isAdult" : false,
        |        "name" : "Для грызунов",
        |        "id" : 12429
        |      },
        |      "currentPage" : 131,
        |      "brand" : null,
        |      "activeSort" : "",
        |      "breadCrumbs" : null
        |    }
        |  },
        |  "searchResultsV2" : {
        |    "searchResultsV2-189805-default-131" : {
        |      "error" : "internal server error"
        |    }
        |  }
        |}
      """.stripMargin

    val layout               = Layout(List(Component.SearchResultsV2("searchResultsV2-189805-default-131" @@ Component.StateId)))
    val catalogDecoderResult = decode[Catalog](catalogRawJson)(Catalog.circeDecoder(layout))

    catalogDecoderResult.isRight shouldBe true
    catalogDecoderResult.map(_.searchResultsV2 shouldBe a[SearchResultsV2.Failure])
  }
}
