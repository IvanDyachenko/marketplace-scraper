package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder

@derive(loggable)
final case class Page(current: Int, total: Int, totalItems: Int) extends Product with Serializable

object Page {
  val MaxValue = 278

  implicit val circeDecoder: Decoder[Page] = List[Decoder[Page]](
    Decoder.forProduct3("currentPage", "totalPages", "totalFound")(apply),
    Decoder.forProduct3("currentSoldOutPage", "totalPages", "totalFound")(apply)
  ).reduceLeft(_ or _)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Page): FreeApplicative[Codec.Field[A, *], Page] =
    (field("currentPage", f(_).current), field("totalPages", f(_).total), field("totalFoundItems", f(_).totalItems)).mapN(apply)
}
