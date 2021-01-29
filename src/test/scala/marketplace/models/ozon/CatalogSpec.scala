package marketplace.models.ozon

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import io.circe.parser.decode

class CatalogSpec extends AnyFlatSpec with Matchers {

  it should "decode Catalog with SearchResultsV2.Failure from a valid JSON" in {
    val catalogRawJson =
      """
        |{
        |   "catalog" : {
        |      "shared" : {
        |         "catalog" : {
        |            "totalPages" : 70,
        |            "context" : "category",
        |            "currentUrl" : "/category/dlya-gryzunov-12429/?page=131",
        |            "categoryPredicted" : false,
        |            "correctedText" : "",
        |            "totalFound" : 2487,
        |            "currentText" : "",
        |            "category" : {
        |               "catalogName" : "",
        |               "imageUrls" : {
        |                  "catalog_logo" : "https://cdn1.ozone.ru/multimedia/1033937063.jpg"
        |               },
        |               "isAdult" : false,
        |               "name" : "Для грызунов",
        |               "id" : 12429
        |            },
        |            "currentPage" : 131,
        |            "brand" : null,
        |            "activeSort" : "",
        |            "breadCrumbs" : null
        |         }
        |      },
        |      "searchResultsV2" : {
        |         "searchResultsV2-189805-default-131" : {
        |            "error" : "internal server error"
        |         }
        |      }
        |   },
        |   "pageInfo" : {
        |      "context" : "ozon",
        |      "layoutId" : 1571,
        |      "url" : "/category/dlya-gryzunov-12429/?layout_container=default&layout_page_index=131&page=131",
        |      "layoutVersion" : 65,
        |      "pageType" : "category",
        |      "ruleId" : 1389
        |   },
        |   "nextPage" : "/category/dlya-gryzunov-12429/?layout_container=no_results&layout_page_index=132&page=131",
        |   "layout" : [
        |      {
        |         "vertical" : "catalog",
        |         "version" : 1,
        |         "stateId" : "searchResultsV2-189805-default-131",
        |         "component" : "searchResultsV2"
        |      }
        |   ]
        |}
      """.stripMargin

    val catalog = decode[Catalog](catalogRawJson)

    catalog.isRight shouldBe true
    catalog.map(_.searchResultsV2 shouldBe a[SearchResultsV2.Failure])
  }
}
