package marketplace.models.yandex.market

import cats.implicits._
import supertagged.TaggedType
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable}

//import java.util.{UUID => juUUID}

object User {
  object UUID extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce {
    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("uuid")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show.filterNot(_ == '-'))
    }
  }
  type UUID = UUID.Type
}
