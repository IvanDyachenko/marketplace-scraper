package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import derevo.circe.decoder
import vulcan.Codec
import vulcan.generic._

@derive(loggable, decoder)
case class Page(currentPage: Int, totalPages: Int, totalFound: Int)

object Page {
  implicit val vulcanCodec: Codec[Page] = Codec.derive[Page]

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Page): FreeApplicative[Codec.Field[A, *], Page] =
    (field("current_page", f(_).currentPage), field("total_pages", f(_).totalPages), field("total_found_items", f(_).totalFound)).mapN(apply)
}
