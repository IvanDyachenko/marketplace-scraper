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
  final case class Sold(numberOfSoldItems: Int) extends Sale

  /** This method calculates number of sold items according to https://gitlab.com/dalytics/analytics/-/issues/26
    */
  def aggregate[C[_]: Foldable](items: C[Item]): Sale = {
    val listOfAddToCarts = Foldable[C].toList(items).map(_.addToCart)

    if (listOfAddToCarts.length > 1)
      listOfAddToCarts.tail
        .foldLeft[(Sale, Item.AddToCart)]((Sale.Unknown, listOfAddToCarts.head)) {
          case ((sale, Item.AddToCart.With(_, prevMaxItems)), addToCart @ Item.AddToCart.With(_, currMaxItems)) =>
            val numberOfSoldItems = sale match {
              case Sale.Unknown                 => 0
              case Sale.Sold(numberOfSoldItems) => numberOfSoldItems
            }
            (Sale.Sold(numberOfSoldItems + 0.max(prevMaxItems - currMaxItems)), addToCart)
          case ((sale, _), addToCart)                                                                           =>
            (sale, addToCart)
        }
        ._1
    else
      Sale.Unknown
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Sale): FreeApplicative[Codec.Field[A, *], Sale] =
    field(
      "numberOfSoldItems",
      f(_) match {
        case Unknown                 => None
        case Sold(numberOfSoldItems) => Some(numberOfSoldItems)
      }
    ).map(_ match {
      case None                    => Unknown
      case Some(numberOfSoldItems) => Sold(numberOfSoldItems)
    })
}
