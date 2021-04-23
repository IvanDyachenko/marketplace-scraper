package net.dalytics.models.ozon

import cats.implicits._
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
sealed trait Button

object Button {
  private[this] sealed trait Type extends EnumEntry with LowerCamelcase with Product with Serializable
  private[this] object Type       extends Enum[Type] with CatsEnum[Type] with CirceEnum[Type] with TethysEnum[Type] with LoggableEnum[Type] {
    val values = findValues

    case object AddToCartButtonWithQuantity extends Type
  }

  @derive(loggable)
  final case class AddToCartWithQuantity(quantity: Int, maxItems: Int) extends Button

  object AddToCartWithQuantity {

    @derive(decoder, tethysReader)
    private[this] final case class Action(quantity: Int) extends AnyVal
    private[this] object Action

    implicit val circeDecoder: Decoder[AddToCartWithQuantity] = Decoder.instance[AddToCartWithQuantity] { (c: HCursor) =>
      lazy val i = c.downField("addToCartButtonWithQuantity")
      for {
        quantity <- i.get[Action]("action").map(_.quantity)
        maxItems <- i.get[Int]("maxItems")
      } yield AddToCartWithQuantity(quantity, maxItems)
    }

    implicit val jsonReader: JsonReader[AddToCartWithQuantity] =
      JsonReader.builder
        .addField[Action]("action")
        .addField[Int]("maxItems")
        .buildReader { case (Action(quantity), maxItems) => apply(quantity, maxItems) }
  }

  implicit val circeDecoder: Decoder[Button] = Decoder.instance[Button] { (c: HCursor) =>
    for {
      // Take a look at test specs to understand why it is necessary
      buttonType   <- (Decoder[Type].at("type").or(Decoder[Type].at("type").at("default")))(c)
      buttonDecoder = buttonType match {
                        case Type.AddToCartButtonWithQuantity => AddToCartWithQuantity.circeDecoder
                      }
      button       <- buttonDecoder.at("default")(c)
    } yield button
  }

  implicit val jsonReader: JsonReader[Option[Button]] =
    JsonReader.builder
      .addField[Option[Button]](
        "default",
        JsonReader.builder
          .addField[Option[AddToCartWithQuantity]](Type.AddToCartButtonWithQuantity.entryName)
          .buildReader(_.widen[Button])
      )
      .buildReader(identity)
}
