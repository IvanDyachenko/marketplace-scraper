package net.dalytics.models.ozon

import derevo.derive
import derevo.circe.decoder
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import tofu.logging.LoggableEnum
import io.circe.{Decoder, HCursor}
import tethys.JsonReader
import tethys.enumeratum.TethysEnum
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.LowerCamelcase

@derive(loggable)
sealed trait Button {
  def `type`: Button.Type
}

object Button {
  sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
  object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with TethysEnum[Type] with LoggableEnum[Type] {
    val values = findValues

    case object AddToCartButtonWithQuantity extends Type
  }

  @derive(loggable)
  final case class AddToCartWithQuantity(private val action: AddToCartWithQuantity.Action, maxItems: Int) extends Button {
    def `type`: Type  = Type.AddToCartButtonWithQuantity
    def quantity: Int = action.quantity
  }

  object AddToCartWithQuantity {

    @derive(loggable, decoder, tethysReader)
    final case class Action(quantity: Int) extends AnyVal

    implicit val circeDecoder: Decoder[AddToCartWithQuantity] = Decoder.instance[AddToCartWithQuantity] { (c: HCursor) =>
      lazy val i = c.downField("addToCartButtonWithQuantity")
      for {
        action   <- i.get[Action]("action")
        maxItems <- i.get[Int]("maxItems")
      } yield AddToCartWithQuantity(action, maxItems)
    }

    implicit val jsonReader: JsonReader[AddToCartWithQuantity] =
      JsonReader.builder
        .addField[AddToCartWithQuantity](
          "addToCartButtonWithQuantity",
          JsonReader.builder
            .addField[Action]("action")
            .addField[Int]("maxItems")
            .buildReader(apply)
        )
        .buildReader(identity)
  }

  implicit val circeDecoder: Decoder[Button] = Decoder.instance[Button] { (c: HCursor) =>
    for {
      buttonType   <- c.get[Type]("type")
      buttonDecoder = buttonType match {
                        case Type.AddToCartButtonWithQuantity => AddToCartWithQuantity.circeDecoder
                      }
      button       <- buttonDecoder.at("default")(c)
    } yield button
  }

  implicit val jsonReader: JsonReader[Button] = JsonReader.builder
    .addField[Type]("type")
    .selectReader {
      // format: off
      case Type.AddToCartButtonWithQuantity => JsonReader.builder.addField[AddToCartWithQuantity]("default").buildReader(identity)
      // format: on
    }
}
