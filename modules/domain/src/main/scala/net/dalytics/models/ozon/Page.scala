package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec
import io.circe.Decoder

@derive(loggable)
sealed trait Page extends Product with Serializable {
  def current: Int
  def total: Int
  def totalItems: Int
}

@derive(loggable)
final case class SearchPage(current: Int, total: Int, totalItems: Int) extends Page

object SearchPage {
  implicit val circeDecoder: Decoder[SearchPage] = Decoder.forProduct3("currentPage", "totalPages", "totalFound")(apply)
}

@derive(loggable)
final case class SoldOutPage(current: Int, total: Int, totalItems: Int) extends Page

object SoldOutPage {
  implicit val circeDecoder: Decoder[SoldOutPage] = Decoder.forProduct3("currentSoldOutPage", "totalPages", "totalFound")(apply)
}

object Page {
  val Top1     = 1
  val Top10    = 10
  val MaxTotal = 278

  implicit final val circeDecoder: Decoder[Page] =
    List[Decoder[Page]](
      Decoder[SearchPage].widen,
      Decoder[SoldOutPage].widen
    ).reduceLeft(_ or _)

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Page): FreeApplicative[Codec.Field[A, *], Page] =
    (field("currentPage", f(_).current), field("totalPages", f(_).total), field("totalFoundItems", f(_).totalItems)).mapN(SearchPage.apply)
}
