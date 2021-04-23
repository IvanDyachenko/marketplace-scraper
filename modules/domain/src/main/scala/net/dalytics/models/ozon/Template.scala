package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import io.circe.{Decoder, HCursor, Json}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase

import net.dalytics.syntax._

@derive(loggable)
final case class Template(states: List[Template.State])

object Template {

  @derive(loggable)
  sealed trait State

  object State {
    private[State] sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
    private[State] object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] {
      val values = findValues

      case object Action          extends Type
      case object Unknown         extends Type
      case object TextSmall       extends Type
      case object MobileContainer extends Type
    }

    @derive(loggable, decoder)
    final object Unknown extends State

    @derive(loggable)
    sealed trait Action extends State

    object Action {
      @derive(loggable, decoder)
      final object Redirect extends Action

      @derive(loggable, decoder)
      final case class UniversalAction(button: Button) extends Action

      @derive(loggable, decoder)
      final case class AddToCartWithCount(minItems: Int, maxItems: Int) extends Action

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[Action]     = deriveConfiguredDecoder[Action].widen
    }

    @derive(loggable)
    sealed trait TextSmall extends State

    object TextSmall {
      @derive(loggable, decoder)
      final object NotDelivered extends TextSmall

      @derive(loggable, decoder)
      final object PremiumPriority extends TextSmall

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[TextSmall]  = deriveConfiguredDecoder[TextSmall].widen
    }

    @derive(loggable)
    final case class MobileContainer(footer: Template) extends State

    object MobileContainer {
      implicit val circeDecoder: Decoder[MobileContainer] = Decoder.forProduct1("footerContainer")(apply)
    }

    implicit val circeDecoder: Decoder[State] = Decoder.instance[State] { (c: HCursor) =>
      for {
        stateType   <- c.get[State.Type]("type").orElse(State.Type.Unknown.asRight)
        stateDecoder = stateType match {
                         case State.Type.Action          => Action.circeDecoder
                         case State.Type.TextSmall       => TextSmall.circeDecoder
                         case State.Type.MobileContainer => MobileContainer.circeDecoder
                         case State.Type.Unknown         => Decoder.const(State.Unknown)
                       }
        state       <- stateDecoder(c)
      } yield state
    }
  }

  implicit val circeDecoder: Decoder[Template] =
    Decoder
      .decodeList(Decoder[State].either(Decoder[Json]))
      .map(ls => Template(ls.flatMap(_.left.toOption)))
}
