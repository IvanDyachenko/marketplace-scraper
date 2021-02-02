package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder
import supertagged.TaggedType

import marketplace.models.{LiftedCats, LiftedCirce, LiftedLoggable, LiftedVulcanCodec}

@derive(loggable)
final case class Catalog(category: Category, currentPage: Int, totalPages: Int, totalFoundItems: Int)

object Catalog {
  object Name extends TaggedType[String] with LiftedCats with LiftedLoggable with LiftedCirce with LiftedVulcanCodec {}
  type Name = Name.Type

  implicit val circeDecoder: Decoder[Catalog] = Decoder.forProduct4("category", "currentPage", "totalPages", "totalFound")(apply)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Catalog): FreeApplicative[Codec.Field[A, *], Catalog] =
    (
      Category.vulcanCodecFieldFA(field)(f(_).category),
      field("currentPage", f(_).currentPage),
      field("totalPages", f(_).totalPages),
      field("totalFoundItems", f(_).totalFoundItems)
    ).mapN(apply)
}
