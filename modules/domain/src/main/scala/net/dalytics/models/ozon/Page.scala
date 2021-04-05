package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder

@derive(loggable)
final case class Page(current: Int, total: Int, totalItems: Int)

object Page {
  val Top1     = 1
  val Top10    = 10
  val MaxTotal = 278

  implicit final val circeDecoder: Decoder[Page] =
    Decoder.forProduct3("currentPage", "totalPages", "totalFound")(apply)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Page): FreeApplicative[Codec.Field[A, *], Page] =
    (field("currentPage", f(_).current), field("totalPages", f(_).total), field("totalFoundItems", f(_).totalItems)).mapN(apply)
}
