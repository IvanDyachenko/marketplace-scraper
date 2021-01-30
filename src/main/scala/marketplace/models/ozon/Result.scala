package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import vulcan.Codec
import vulcan.generic._

@derive(loggable)
@AvroNamespace("ozon.models")
final case class Result(layout: Layout, catalog: Catalog) {

  def isFailure: Boolean = catalog.searchResultsV2 match {
    case _: SearchResultsV2.Failure => true
    case _                          => false
  }
}

object Result {
  implicit val circeDecoder: Decoder[Result] = new Decoder[Result] {
    final def apply(c: HCursor): Decoder.Result[Result] =
      for {
        layout  <- c.get[Layout]("layout")
        catalog <- c.get[Catalog]("catalog")(Catalog.circeDecoder(layout))
      } yield Result(layout, catalog)
  }

  implicit val vulcanCodec: Codec[Result] = Codec.derive[Result]
}
