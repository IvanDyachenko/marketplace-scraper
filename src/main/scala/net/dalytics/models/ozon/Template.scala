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
final case class Template(state: List[Template.State])

object Template {

  @derive(loggable)
  sealed trait State { def `type`: State.Type }

  object State {

    sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
    object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with LoggableEnum[Type] {
      val values = findValues

      case object Action          extends Type
      case object Unknown         extends Type
      case object MobileContainer extends Type
    }

    sealed trait Id extends EnumEntry with LowerCamelcase with Product with Serializable
    object Id       extends Enum[Id] with CatsEnum[Id] with CirceEnum[Id] with LoggableEnum[Id] {
      val values = findValues

      case object Name               extends Id
      case object Price              extends Id
      case object PricePerUnit       extends Id
      case object UniversalAction    extends Id
      case object AddToCartWithCount extends Id
    }

    @derive(loggable)
    sealed trait Action extends State {
      def id: State.Id
      val `type`: State.Type = State.Type.Action
    }

    @derive(loggable, decoder)
    final object Unknown extends State {
      val `type`: State.Type = State.Type.Unknown
    }

    @derive(loggable)
    final case class MobileContainer(left: Template, content: Template, footer: Template) extends State {
      val `type`: State.Type            = State.Type.Action
      def addToCart: Option[(Int, Int)] = left.addToCart.orElse(content.addToCart).orElse(footer.addToCart)
    }

    object Action {

      @derive(loggable, decoder)
      final case class UniversalAction(button: UniversalAction.Button) extends Action {
        val id: State.Id = State.Id.UniversalAction
      }

      @derive(loggable, decoder)
      final case class AddToCartWithCount(minItems: Int, maxItems: Int) extends Action {
        val id: State.Id = State.Id.AddToCartWithCount
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

          implicit val circeDecoder: Decoder[Button] = List[Decoder[Button]](Decoder[AddToCartWithQuantity].widen).reduceLeft(_ or _)
        }
      }

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[Action]     = deriveConfiguredDecoder[Action].widen
    }

    object MobileContainer {
      implicit val circeDecoder: Decoder[MobileContainer] = Decoder.forProduct3("leftContainer", "contentContainer", "footerContainer")(apply)
    }

    implicit val circeDecoder: Decoder[State] = Decoder.instance[State] { (c: HCursor) =>
      for {
        t      <- c.get[State.Type]("type").fold(_ => State.Type.Unknown.asRight, Right(_))
        decoder = t match {
                    case State.Type.Action          => Action.circeDecoder
                    case State.Type.MobileContainer => MobileContainer.circeDecoder
                    case State.Type.Unknown         => Decoder[Json].map(_ => State.Unknown)
                  }
        state  <- decoder(c).fold(_ => State.Unknown.asRight, Right(_))
      } yield state
    }
  }

  implicit final class TemplateOps(private val template: Template) extends AnyVal {
    def addToCart: Option[(Int, Int)] =
      template.state.collectFirst {
        case State.Action.AddToCartWithCount(minItems, maxItems)                                                         =>
          minItems -> maxItems
        case State.Action.UniversalAction(State.Action.UniversalAction.Button.AddToCartWithQuantity(quantity, maxItems)) =>
          quantity -> maxItems
        case mobileContainer: State.MobileContainer if mobileContainer.addToCart.isDefined                               =>
          mobileContainer.addToCart.get
      }
  }

  implicit val circeDecoder: Decoder[Template] =
    Decoder
      .decodeList(Decoder[State].either(Decoder[Json]))
      .map(ls => Template(ls.flatMap(_.left.toOption)))
}
