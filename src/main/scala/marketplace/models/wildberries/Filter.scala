package marketplace.models.wildberries

import cats.data.{Validated, ValidatedNel}
import org.http4s.{ParseFailure, QueryParam, QueryParamCodec, QueryParameterKey, QueryParameterValue}

final case class Filter private (value: String)

object Filter {
  type Filters = List[Filter]

  implicit val queryParam = new QueryParam[Filters] with QueryParamCodec[Filters] {
    val key: QueryParameterKey = QueryParameterKey("filters")

    def encode(filters: Filters): QueryParameterValue =
      QueryParameterValue(filters.map(_.value).mkString(";"))

    def decode(queryParameterValue: QueryParameterValue): ValidatedNel[ParseFailure, Filters] =
      Validated.valid(queryParameterValue.value.split(";").toList.map(apply))
  }
}
