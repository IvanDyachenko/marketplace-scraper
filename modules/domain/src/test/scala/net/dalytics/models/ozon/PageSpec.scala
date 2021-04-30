package net.dalytics.models.ozon

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import tethys._
import tethys.jackson._

class PageSpec extends AnyFlatSpec with Matchers with EitherValues {

  it should "decode Page from a valid JSON (1) (tethys)" in {
    val pageRawJson =
      """
        |{
        |  "currentPage": 3,
        |  "totalFound": 178,
        |  "totalPages": 5
        |}
      """.stripMargin

    val page = pageRawJson.jsonAs[Page].value

    page should be(Page(3, 5, 178))
  }

  it should "decode Page from a valid JSON (2) (tethys)" in {
    val pageRawJson =
      """
        |{
        |  "currentSoldOutPage": 1,
        |  "totalFound": 122,
        |  "totalPages": 4
        |}
      """.stripMargin

    val page = pageRawJson.jsonAs[Page].value

    page should be(Page(1, 4, 122))
  }
}
