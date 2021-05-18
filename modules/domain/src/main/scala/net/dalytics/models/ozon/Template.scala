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
private[ozon] final case class Template(states: List[Template.State]) {
  def isNew: Boolean        = isLabeled(Template.State.Label.Type.New)
  def isBestseller: Boolean = isLabeled(Template.State.Label.Type.Bestseller)

  private def isLabeled(label: Template.State.Label.Type): Boolean =
    states
      .collectFirst {
        case Template.State.Label(items) if items.contains(label)                   => true
        case Template.State.MobileContainer(content, _) if content.isLabeled(label) => true
      }
      .getOrElse(false)
}

private[ozon] object Template {

  @derive(loggable)
  sealed trait State

  object State {
    private[State] sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
    private[State] object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with TethysEnum[Type] with LoggableEnum[Type] {
      val values = findValues

      case object Unknown         extends Type
      case object Action          extends Type
      case object Label           extends Type
      case object TextSmall       extends Type
      case object MobileContainer extends Type
    }

    @derive(loggable)
    final object Unknown extends State {
      implicit val jsonReader: JsonReader[Unknown.type] = JsonReader.builder.addField[Option[String]]("type").buildReader(_ => Unknown)
    }

    @derive(loggable)
    sealed trait Action extends State

    object Action {
      @derive(loggable, decoder)
      final object Unknown extends Action {
        implicit val jsonReader: JsonReader[Unknown.type] = JsonReader.builder.addField[Option[String]]("id").buildReader(_ => Unknown)
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
          .addField[Option[String]]("id")
          .selectReader {
            case Some("redirect")           => JsonReader[Redirect.type]
            case Some("universalAction")    => JsonReader[UniversalAction]
            case Some("addToCartWithCount") => JsonReader[AddToCartWithCount]
            case _                          => JsonReader[Unknown.type]
          }
    }

    @derive(loggable, decoder, tethysReader)
    final case class Label(items: List[Label.Type]) extends State

    object Label {
      sealed abstract class Type(override val entryName: String) extends EnumEntry with Product with Serializable

      object Type extends Enum[Type] with CatsEnum[Type] with LoggableEnum[Type] {
        val values = findValues

        case object Unknown    extends Type("")
        case object New        extends Type("Новинка")
        case object Bestseller extends Type("Бестселлер")

        implicit val circeDecoder: Decoder[Type] =
          Decoder.forProduct1[Type, Option[String]]("title") { titleOpt =>
            val entryName = titleOpt.getOrElse(Type.Unknown.entryName)
            Type.withNameInsensitiveOption(entryName).getOrElse(Type.Unknown)
          }

        implicit val jsonReader: JsonReader[Type] =
          JsonReader.builder
            .addField[Option[String]]("title")
            .buildReader { titleOpt =>
              val entryName = titleOpt.getOrElse(Type.Unknown.entryName)
              Type.withNameInsensitiveOption(entryName).getOrElse(Type.Unknown)
            }
      }
    }

    @derive(loggable)
    sealed trait TextSmall extends State

    object TextSmall {
      @derive(loggable, decoder)
      final object Unknown extends TextSmall {
        implicit val jsonReader: JsonReader[Unknown.type] = JsonReader.builder.addField[Option[String]]("id").buildReader(_ => Unknown)
      }

      @derive(loggable, decoder)
      final object NotDelivered extends TextSmall

      @derive(loggable, decoder)
      final object PremiumPriority extends TextSmall

      implicit val circeDecoderConfig: Configuration = Configuration(Predef.identity, _.decapitalize, false, Some("id"))
      implicit val circeDecoder: Decoder[TextSmall]  = deriveConfiguredDecoder[TextSmall].widen

      implicit val jsonReader: JsonReader[TextSmall] =
        JsonReader.builder
          .addField[Option[String]]("id")
          .buildReader {
            case Some("notDelivered")    => NotDelivered
            case Some("premiumPriority") => PremiumPriority
            case _                       => Unknown
          }
    }

    @derive(loggable, decoder, tethysReader)
    final case class MobileContainer(contentContainer: Template, footerContainer: Template) extends State

    implicit val circeDecoder: Decoder[State] = Decoder.instance[State] { (c: HCursor) =>
      for {
        stateType   <- c.get[Type]("type").orElse(Type.Unknown.asRight)
        stateDecoder = stateType match {
                         case Type.Action          => Decoder[Action]
                         case Type.Label           => Decoder[Label]
                         case Type.TextSmall       => Decoder[TextSmall]
                         case Type.MobileContainer => Decoder[MobileContainer]
                         case Type.Unknown         => Decoder.const(State.Unknown)
                       }
        state       <- stateDecoder(c)
      } yield state
    }

    implicit val jsonReader: JsonReader[State] =
      JsonReader.builder.addField[Option[String]]("type").selectReader { typeOpt =>
        val entryName = typeOpt.getOrElse(Type.Unknown.entryName)
        val stateType = Type.withNameOption(entryName).getOrElse(Type.Unknown)

        stateType match {
          case Type.Action          => JsonReader[Action]
          case Type.Label           => JsonReader[Label]
          case Type.TextSmall       => JsonReader[TextSmall]
          case Type.MobileContainer => JsonReader[MobileContainer]
          case Type.Unknown         => JsonReader[Unknown.type]
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
