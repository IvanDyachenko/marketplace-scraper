package marketplace.models.ozon

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

import marketplace.syntax._

@derive(loggable)
final case class Template(state: List[Template.State])

object Template {

  @derive(loggable)
  sealed trait State { def id: State.Id }

  object State {

    sealed trait Id extends EnumEntry with LowerCamelcase with Product with Serializable
    object Id       extends Enum[Id] with CatsEnum[Id] with CirceEnum[Id] with LoggableEnum[Id] {
      val values = findValues

      case object Name               extends Id
      case object Price              extends Id
      case object PricePerUnit       extends Id
      case object UniversalAction    extends Id
      case object AddToCartWithCount extends Id
    }

    @derive(loggable, decoder)
    final case class UniversalAction(button: UniversalAction.Button) extends State {
      val id: State.Id = State.Id.UniversalAction
    }

    @derive(loggable, decoder)
    final case class AddToCartWithCount(minItems: Int, maxItems: Int) extends State {
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
    implicit val circeDecoder: Decoder[State]      = deriveConfiguredDecoder[State]
  }

  implicit final class TemplateOps(private val template: Template) extends AnyVal {
    def addToCart: Option[(Int, Int)] =
      template.state.collectFirst {
        case State.AddToCartWithCount(minItems, maxItems)                                                  => minItems -> maxItems
        case State.UniversalAction(State.UniversalAction.Button.AddToCartWithQuantity(quantity, maxItems)) => quantity -> maxItems
      }
  }

  implicit val circeDecoder: Decoder[Template] = Decoder
    .decodeList(Decoder[State].either(Decoder[Json]))
    .map(ls => Template.apply(ls.flatMap(_.left.toOption)))
}
