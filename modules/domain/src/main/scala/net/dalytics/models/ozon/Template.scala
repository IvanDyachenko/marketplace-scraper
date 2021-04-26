package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import io.circe.{Decoder, HCursor}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import tethys.JsonReader
import tethys.enumeratum.TethysEnum
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
    private[State] object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with TethysEnum[Type] with LoggableEnum[Type] {
      val values = findValues

      case object Action          extends Type
      case object Unknown         extends Type
      case object TextSmall       extends Type
      case object MobileContainer extends Type
    }

    @derive(loggable)
    final object Unknown extends State {
      implicit val jsonReader: JsonReader[Unknown.type] = JsonReader.builder.addField[String]("type").buildReader(_ => Unknown)
    }

    @derive(loggable)
    sealed trait Action extends State

    object Action {
      @derive(loggable, decoder)
      final object Unknown extends Action {
        implicit val jsonReader: JsonReader[Unknown.type] = JsonReader.builder.addField[String]("id").buildReader(_ => Unknown)
      }

      @derive(loggable, decoder)
      final object Redirect extends Action {
        implicit val jsonReader: JsonReader[Redirect.type] = JsonReader.builder.addField[String]("id").buildReader(_ => Redirect)
      }

      @derive(loggable, decoder, tethysReader)
      final case class UniversalAction(button: Button) extends Action

      @derive(loggable, decoder, tethysReader)
      final case class AddToCartWithCount(minItems: Int, maxItems: Int) extends Action

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[Action]     = deriveConfiguredDecoder[Action].widen

      implicit val jsonReader: JsonReader[Action] =
        JsonReader.builder
          .addField[String]("id")
          .selectReader {
            case "redirect"           => JsonReader[Redirect.type]
            case "universalAction"    => JsonReader[UniversalAction]
            case "addToCartWithCount" => JsonReader[AddToCartWithCount]
            case _                    => JsonReader[Unknown.type]
          }
    }

    @derive(loggable)
    sealed trait TextSmall extends State

    object TextSmall {
      @derive(loggable, decoder)
      final object Unknown extends TextSmall {
        implicit val jsonReader: JsonReader[Unknown.type] = JsonReader.builder.addField[String]("id").buildReader(_ => Unknown)
      }

      @derive(loggable, decoder)
      final object NotDelivered extends TextSmall

      @derive(loggable, decoder)
      final object PremiumPriority extends TextSmall

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[TextSmall]  = deriveConfiguredDecoder[TextSmall].widen

      implicit val jsonReader: JsonReader[TextSmall] =
        JsonReader.builder
          .addField[String]("id")
          .buildReader {
            case "notDelivered"    => NotDelivered
            case "premiumPriority" => PremiumPriority
            case _                 => Unknown
          }
    }

    @derive(loggable, decoder, tethysReader)
    final case class MobileContainer(footerContainer: Template) extends State

    implicit val circeDecoder: Decoder[State] = Decoder.instance[State] { (c: HCursor) =>
      for {
        stateType   <- c.get[Type]("type").orElse(Type.Unknown.asRight)
        stateDecoder = stateType match {
                         case Type.Action          => Decoder[Action]
                         case Type.TextSmall       => Decoder[TextSmall]
                         case Type.MobileContainer => Decoder[MobileContainer]
                         case Type.Unknown         => Decoder.const(State.Unknown)
                       }
        state       <- stateDecoder(c)
      } yield state
    }

    implicit val jsonReader: JsonReader[State] =
      JsonReader.builder.addField[String]("type").selectReader { typeName =>
        Type.withNameOption(typeName) match {
          case Some(Type.Action)          => JsonReader[Action]
          case Some(Type.TextSmall)       => JsonReader[TextSmall]
          case Some(Type.MobileContainer) => JsonReader[MobileContainer]
          case _                          => JsonReader[Unknown.type]
        }
      }
  }

  implicit val circeDecoder: Decoder[Template] =
    Decoder
      .decodeList(Decoder[State].either(Decoder.const(State.Unknown)))
      .map(ls => Template(ls.flatMap(_.left.toOption)))

  implicit val jsonReader: JsonReader[Template] =
    JsonReader
      .iterableReader[State, List]
      .map(apply)
}
