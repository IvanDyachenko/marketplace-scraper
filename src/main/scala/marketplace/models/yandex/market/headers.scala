package marketplace.models.yandex.market

package headers {
  import cats.implicits._
  import org.http4s.{Header, HeaderKey, ParseFailure, ParseResult}
  import org.http4s.util.Writer
  import org.typelevel.ci.CIString

  final case class `X-Region-Id`(geoId: Region.GeoId) extends Header.Parsed {
    def key                                      = `X-Region-Id`
    def renderValue(writer: Writer): writer.type = writer.append(geoId.show)
  }

  object `X-Region-Id` extends HeaderKey.Singleton {
    type HeaderT = `X-Region-Id`

    def name: CIString = CIString("X-Region-Id")

    def parse(s: String): ParseResult[`X-Region-Id`] =
      Region.GeoId(s) match {
        case Some(value) => Right(`X-Region-Id`(value))
        case _           => Left(ParseFailure.apply(s, "Invalid region identifier"))
      }

    def matchHeader(header: Header): Option[`X-Region-Id`] =
      if (header.name == name) Region.GeoId(header.value).map(`X-Region-Id`(_))
      else None
  }

  final case class `X-Device-Type`(deviceType: String) extends Header.Parsed {
    def key                                      = `X-Device-Type`
    def renderValue(writer: Writer): writer.type = writer.append(deviceType)
  }

  object `X-Device-Type` extends HeaderKey.Singleton {
    type HeaderT = `X-Device-Type`

    def name: CIString = CIString("X-Device-Type")

    def parse(s: String): ParseResult[`X-Device-Type`] = Right(`X-Device-Type`(s))

    def matchHeader(header: Header): Option[`X-Device-Type`] =
      if (header.name == name) Some(`X-Device-Type`(header.value))
      else None
  }

  final case class `X-Platform`(platform: String) extends Header.Parsed {
    def key                                      = `X-Platform`
    def renderValue(writer: Writer): writer.type = writer.append(platform)
  }

  object `X-Platform` extends HeaderKey.Singleton {
    type HeaderT = `X-Platform`

    def name: CIString = CIString("X-Platform")

    def parse(s: String): ParseResult[`X-Platform`] = Right(`X-Platform`(s))

    def matchHeader(header: Header): Option[`X-Platform`] =
      if (header.name == name) Some(`X-Platform`(header.value))
      else None
  }

  final case class `X-App-Version`(appVersion: String) extends Header.Parsed {
    def key                                      = `X-App-Version`
    def renderValue(writer: Writer): writer.type = writer.append(appVersion)
  }

  object `X-App-Version` extends HeaderKey.Singleton {
    type HeaderT = `X-App-Version`

    def name: CIString = CIString("X-App-Version")

    def parse(s: String): ParseResult[`X-App-Version`] = Right(`X-App-Version`(s))

    def matchHeader(header: Header): Option[`X-App-Version`] =
      if (header.name == name) Some(`X-App-Version`(header.value))
      else None
  }
}
