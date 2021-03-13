package net.dalytics.models.ozon

import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec

@derive(loggable)
final case class Sale(numberOfSoldItems: Option[Int] = None)

object Sale {
  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => Sale): FreeApplicative[Codec.Field[A, *], Sale] =
    (field("numberOfSoldItems", f(_).numberOfSoldItems)).map(apply)
}
