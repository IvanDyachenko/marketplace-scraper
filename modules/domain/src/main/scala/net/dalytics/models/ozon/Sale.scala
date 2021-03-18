package net.dalytics.models.ozon

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
