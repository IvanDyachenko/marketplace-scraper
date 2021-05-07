package net.dalytics.models.ozon

import cats.implicits._
import cats.free.FreeApplicative
import derevo.derive
import tofu.logging.derivation.loggable
import vulcan.Codec

@derive(loggable)
sealed trait AddToCart

object AddToCart {
  final object Redirect                               extends AddToCart
  final object PremiumOnly                            extends AddToCart
  final object Unavailable                            extends AddToCart
  final case class With(minItems: Int, maxItems: Int) extends AddToCart

  def apply(template: Template): Option[AddToCart] = template.addToCart

  implicit final class TemplateOps(private val template: Template) extends AnyVal {
    import net.dalytics.models.ozon.Template.State.{Action, MobileContainer, TextSmall}
    import net.dalytics.models.ozon.Template.State.Action.{AddToCartWithCount, UniversalAction}

    def addToCart: Option[AddToCart] = template.states.collectFirst {
      case Action.Redirect                                                   => Redirect
      case TextSmall.PremiumPriority | TextSmall.NotDelivered                => PremiumOnly
      case AddToCartWithCount(minItems, maxItems)                            => With(minItems, maxItems)
      case UniversalAction(Button.AddToCartWithQuantity(quantity, maxItems)) => With(quantity, maxItems)
      case MobileContainer(_, footer) if footer.addToCart.isDefined          => footer.addToCart.get
    }
  }

  private[models] def vulcanCodecFieldFA[A](field: Codec.FieldBuilder[A])(f: A => AddToCart): FreeApplicative[Codec.Field[A, *], AddToCart] =
    (
      field(
        "addToCartIsRedirect",
        f(_) match {
          case Redirect                 => Some(true)
          case With(_, _) | PremiumOnly => Some(false)
          case Unavailable              => None
        }
      ),
      field(
        "addToCartIsPremiumOnly",
        f(_) match {
          case PremiumOnly           => Some(true)
          case With(_, _) | Redirect => Some(false)
          case Unavailable           => None
        }
      ),
      field(
        "addToCartMinItems",
        f(_) match {
          case With(minItems, _) => Some(minItems)
          case _                 => None
        }
      ),
      field(
        "addToCartMaxItems",
        f(_) match {
          case With(_, maxItems) => Some(maxItems)
          case _                 => None
        }
      )
    ).mapN {
      case (Some(true), Some(false), None, None)                      => Redirect
      case (Some(false), Some(true), None, None)                      => PremiumOnly
      case (Some(false), Some(false), Some(minItems), Some(maxItems)) => With(minItems, maxItems)
      case _                                                          => Unavailable
    }
}
