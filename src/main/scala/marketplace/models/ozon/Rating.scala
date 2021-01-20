package marketplace.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder

@derive(loggable)
final case class Rating(value: Double, count: Int)

object Rating {
  implicit val circeDecoder: Decoder[Rating] = Decoder.forProduct2("rating", "countItems")(apply)

  def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Rating): FreeApplicative[Codec.Field[A, *], Rating] =
    (field("ratingValue", f(_).value), field("ratingCount", f(_).count)).mapN(apply)
}
