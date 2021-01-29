package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}

@derive(loggable)
final case class Result(layout: Layout, catalog: Catalog)

object Result {
  implicit val circeDecoder: Decoder[Result] = new Decoder[Result] {
    final def apply(c: HCursor): Decoder.Result[Result] =
      for {
        layout  <- c.get[Layout]("layout")
        catalog <- c.get[Catalog]("catalog")(Catalog.circeDecoder(layout))
      } yield Result(layout, catalog)
  }
}
