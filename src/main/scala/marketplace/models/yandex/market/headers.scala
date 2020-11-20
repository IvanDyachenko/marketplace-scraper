package marketplace.models.yandex.market

package headers {
  import cats.implicits._
  import org.http4s.syntax.string._
  import org.http4s.{Header, HeaderKey, ParseFailure, ParseResult}
  import org.http4s.util.{CaseInsensitiveString, Writer}

  final case class `X-Region-Id`(geoId: Region.GeoId) extends Header.Parsed {
    def key                                      = `X-Region-Id`
    def renderValue(writer: Writer): writer.type = writer.append(geoId.show)
  }

  object `X-Region-Id` extends HeaderKey.Singleton {
    type HeaderT = `X-Region-Id`

    def name: CaseInsensitiveString = "X-Region-Id".ci

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

    def name: CaseInsensitiveString = "X-Device-Type".ci

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

    def name: CaseInsensitiveString = "X-Platform".ci

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

    def name: CaseInsensitiveString = "X-App-Version".ci

    def parse(s: String): ParseResult[`X-App-Version`] = Right(`X-App-Version`(s))

    def matchHeader(header: Header): Option[`X-App-Version`] =
      if (header.name == name) Some(`X-App-Version`(header.value))
      else None
  }
}
