package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import vulcan.generic._
import io.circe.Decoder
import tofu.logging.LoggableEnum
import org.http4s.{QueryParamEncoder, QueryParamKeyLike, QueryParameterKey, QueryParameterValue}
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase

@derive(loggable)
sealed trait SearchFilter {
  def key: SearchFilter.Key
}

@derive(loggable)
@AvroNamespace("ozon.models")
final case class BrandFilter(brandId: Brand.Id) extends SearchFilter {
  val key = SearchFilter.Key.Brand
}

object BrandFilter {
  implicit val circeDecoder: Decoder[BrandFilter] = Decoder.forProduct1("key")(apply)
  implicit val vulcanCodec: Codec[BrandFilter]    = Codec.derive[BrandFilter]
}

object SearchFilter {
  sealed trait Key extends EnumEntry with LowerCamelcase with Product with Serializable
  object Key       extends Enum[Key] with CatsEnum[Key] with CirceEnum[Key] with LoggableEnum[Key] {
    val values = findValues

    case object Brand extends Key

    implicit val queryParamKeyLike = new QueryParamKeyLike[Key] {
      def getKey(t: Key): QueryParameterKey = QueryParameterKey(t.show)
    }
  }

  implicit val queryParam = new QueryParamEncoder[SearchFilter] {
    def encode(filter: SearchFilter): QueryParameterValue = QueryParameterValue(filter match {
      case BrandFilter(brandId) => brandId.show
    })
  }

  implicit val circeDecoder: Decoder[SearchFilter] =
    List[Decoder[SearchFilter]](
      Decoder[BrandFilter].widen
    ).reduceLeft(_ or _)

  implicit val vulcanCodec: Codec[SearchFilter] = Codec.union[SearchFilter](alt => alt[BrandFilter])
}
