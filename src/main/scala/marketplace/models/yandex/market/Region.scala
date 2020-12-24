package marketplace.models.yandex.market

import cats.Show
import supertagged.{TaggedOps, TaggedType, TaggedType0}
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.UpperSnakecase
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import tofu.logging.{Loggable, LoggableEnum}
import derevo.derive
import tofu.logging.derivation.loggable
import org.http4s.{QueryParam, QueryParamEncoder, QueryParameterKey, QueryParameterValue}

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

/** Информация о регионе запроса.
  *
  * @param id      Идентификатор региона.
  * @param name    Наименование региона.
  * @param type    Тип региона.
  * @param country Страна, к которой относится регион.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
case class Region(id: Region.GeoId, name: String, `type`: Region.Type, country: Country)

object Region {

  /** Идентификатор региона, для которого нужно получить информацию о предложениях.
    */
  object GeoId extends TaggedType0[Int] {
    def apply(value: Int): Type = TaggedOps(this)(value)

    def apply(value: String): Option[Type]   =
      try Some(apply(value.toInt))
      catch { case _: NumberFormatException => None }

    implicit val show: Show[Type]            = Show.fromToString
    implicit val loggable: Loggable[Type]    = TaggedOps(this).lift
    implicit val circeDecoder: Decoder[Type] = TaggedOps(this).lift
    implicit val vulcanCodec: Codec[Type]    = TaggedOps(this).lift

    implicit val queryParam = new QueryParam[Type] with QueryParamEncoder[Type] {
      val key                                      = QueryParameterKey("geo_id")
      def encode(value: Type): QueryParameterValue = QueryParameterValue(value.toString)
    }
  }
  type GeoId = GeoId.Type

  /** Тип региона.
    */
  @AvroNamespace("yandex.market.models")
  sealed trait Type extends EnumEntry with UpperSnakecase

  object Type extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] with VulcanEnum[Type] {

    case object Continent                 extends Type // Континент.
    case object Region                    extends Type // Регион.
    case object Country                   extends Type // Страна.
    case object CountryDistrict           extends Type // Федеральный округ.
    case object SubjectFederation         extends Type // Субъект федерации.
    case object City                      extends Type // Город.
    case object Village                   extends Type // Село.
    case object CityDistrict              extends Type // Район города.
    case object MetroStation              extends Type // Станция метро.
    case object SubjectFederationDistrict extends Type // Район субъекта федерации.
    case object Airport                   extends Type // Аэропорт.
    case object OverseasTerritory         extends Type // Отдельная территория какого-либо государства.
    case object SecondaryDistrict         extends Type // Район города второго уровня.
    case object MonorailStation           extends Type // Станция монорельса.
    case object RuralSettlement           extends Type // Сельское поселение.
    case object Other                     extends Type // Другой тип населенного пункта.

    val values = findValues
  }

  implicit val circeDecoder: Decoder[Region] = deriveDecoder
  implicit val avroCodec: Codec[Region]      = Codec.derive[Region]
}

/** Страна, к которой относится регион.
  *
  * @param id   Код страны.
  * @param name Наименование страны.
  */
@derive(loggable)
@AvroNamespace("yandex.market.models")
case class Country(id: Country.CountryId, name: String)

object Country {

  /** Код страны.
    */
  object CountryId extends TaggedType[Int] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type CountryId = CountryId.Type

  implicit val circeDecoder: Decoder[Country] = deriveDecoder
  implicit val avroCodec: Codec[Country]      = Codec.derive[Country]
}
