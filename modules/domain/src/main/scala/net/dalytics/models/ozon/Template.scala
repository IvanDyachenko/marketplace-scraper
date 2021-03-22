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
  sealed trait State {
    def `type`: State.Type
  }

  object State {
    sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
    object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] {
      val values = findValues

      case object Action          extends Type
      case object Unknown         extends Type
      case object TextSmall       extends Type
      case object MobileContainer extends Type
    }

    sealed trait Id extends EnumEntry with LowerCamelcase with Product with Serializable
    object Id       extends Enum[Id] with CatsEnum[Id] with CirceEnum[Id] with LoggableEnum[Id] {
      val values = findValues

      case object Name               extends Id
      case object Price              extends Id
      case object PricePerUnit       extends Id
      case object Redirect           extends Id
      case object PremiumPriority    extends Id
      case object UniversalAction    extends Id
      case object AddToCartWithCount extends Id
    }

    @derive(loggable, decoder)
    final object Unknown extends State {
      val `type`: State.Type = State.Type.Unknown
    }

    @derive(loggable)
    sealed trait Action extends State {
      def id: State.Id
      val `type`: State.Type = State.Type.Action
    }

    object Action {
      @derive(loggable, decoder)
      final object Redirect extends Action {
        val id: State.Id = State.Id.Redirect
      }

      @derive(loggable, decoder)
      final case class AddToCartWithCount(minItems: Int, maxItems: Int) extends Action {
        val id: State.Id = State.Id.AddToCartWithCount
      }

      @derive(loggable, decoder)
      final case class UniversalAction(button: UniversalAction.Button) extends Action {
        val id: State.Id = State.Id.UniversalAction
      }

      object UniversalAction {
        @derive(loggable)
        sealed trait Button

        object Button {
          @derive(loggable)
          final case class AddToCartWithQuantity(quantity: Int, maxItems: Int) extends Button

          object AddToCartWithQuantity {
            implicit val circeDecoder: Decoder[AddToCartWithQuantity] = Decoder.instance[AddToCartWithQuantity] { (c: HCursor) =>
              lazy val i = c.downField("default").downField("addToCartButtonWithQuantity")
              for {
                quantity <- i.downField("action").get[Int]("quantity")
                maxItems <- i.get[Int]("maxItems")
              } yield AddToCartWithQuantity(quantity, maxItems)
            }
          }

          implicit val circeDecoder: Decoder[Button] = List[Decoder[Button]](
            Decoder[AddToCartWithQuantity].widen
          ).reduceLeft(_ or _)
        }
      }

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[Action]     = deriveConfiguredDecoder[Action].widen
    }

    @derive(loggable)
    sealed trait TextSmall extends State {
      def id: State.Id
      val `type`: State.Type = State.Type.TextSmall
    }

    object TextSmall {
      @derive(loggable, decoder)
      final object PremiumPriority extends TextSmall {
        val id: State.Id = State.Id.PremiumPriority
      }

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[TextSmall]  = deriveConfiguredDecoder[TextSmall].widen
    }

    @derive(loggable)
    final case class MobileContainer(left: Template, content: Template, footer: Template) extends State {
      val `type`: State.Type = State.Type.Action
    }

    object MobileContainer {
      implicit val circeDecoder: Decoder[MobileContainer] = Decoder.forProduct3("leftContainer", "contentContainer", "footerContainer")(apply)
    }

    implicit val circeDecoder: Decoder[State] = Decoder.instance[State] { (c: HCursor) =>
      for {
        stateType   <- c.get[State.Type]("type").fold(_ => State.Type.Unknown.asRight, Right(_))
        stateDecoder = stateType match {
                         case State.Type.Action          => Action.circeDecoder
                         case State.Type.TextSmall       => TextSmall.circeDecoder
                         case State.Type.MobileContainer => MobileContainer.circeDecoder
                         case State.Type.Unknown         => Decoder[Json].map(_ => State.Unknown)
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
