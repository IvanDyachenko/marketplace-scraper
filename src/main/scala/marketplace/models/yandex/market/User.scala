package marketplace.models.yandex.market

import cats.Show
import cats.implicits._
import vulcan.Codec
import io.circe.Decoder
import supertagged.TaggedType
import tofu.logging.Loggable
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

object User {

  object UUID extends TaggedType[String] {
    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = lift
    implicit val circeDecoder: Decoder[Type] = lift
    implicit val avroCodec: Codec[Type]      = lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("uuid")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.show)
    }
  }
  type UUID = UUID.Type

}
