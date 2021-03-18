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

  def aggregate[C[_]: Foldable](items: C[Item]): Sale = {
    val listOfMaxItems = Foldable[C].toList(items).map(_.addToCart).collect { case Item.AddToCart.With(_, maxItems) =>
      maxItems
    }

    if (listOfMaxItems.length > 1) {
      val numberOfSoldItems =
        listOfMaxItems.tail
          .foldLeft((0, listOfMaxItems.head)) { case ((numberOfSoldItems, prevMaxItems), currMaxItems) =>
            (numberOfSoldItems + 0.max(currMaxItems - prevMaxItems), currMaxItems)
          }
          ._1
      Sale.Sold(numberOfSoldItems)
    } else
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
