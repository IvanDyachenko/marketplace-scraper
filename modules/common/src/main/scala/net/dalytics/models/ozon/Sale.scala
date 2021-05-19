package net.dalytics.models.ozon

import cats.Foldable
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec

@derive(loggable)
sealed trait Sale

object Sale {

  @derive(loggable)
  final object Unknown extends Sale

  @derive(loggable)
  final case class Sold(count: Int) extends Sale

  /** This method calculates number of sold items according to https://gitlab.com/dalytics/analytics/-/issues/26
    */
  def from[C[_]: Foldable](changelog: C[Item]): Sale = {
    val log = Foldable[C].toList(changelog).map(_.addToCart)

    if (log.length > 1) {
      log.tail
        .foldLeft[(Sale, AddToCart)]((Sale.Unknown, log.head)) {
          case ((sale, AddToCart.With(_, prevMaxItems)), addToCart @ AddToCart.With(_, currMaxItems)) =>
            val count = sale match {
              case Sale.Unknown     => 0
              case Sale.Sold(count) => count
            }
            (Sale.Sold(count + 0.max(prevMaxItems - currMaxItems)), addToCart)
          case ((sale, _), addToCart)                                                                 =>
            (sale, addToCart)
        }
        ._1
    } else
      Sale.Unknown
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Sale): FreeApplicative[Codec.Field[A, *], Sale] =
    field(
      "numberOfSoldItems",
      f(_) match {
        case Unknown     => None
        case Sold(count) => Some(count)
      }
    ).map(_ match {
      case None        => Unknown
      case Some(count) => Sold(count)
    })
}
