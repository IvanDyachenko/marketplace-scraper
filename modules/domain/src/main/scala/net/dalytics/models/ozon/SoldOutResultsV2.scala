package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import tethys.JsonReader

@derive(loggable)
sealed trait SoldOutResultsV2

object SoldOutResultsV2 {

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SoldOutResultsV2

  @derive(loggable)
  final case class Success(items: List[Item]) extends SoldOutResultsV2

  object Success {
    implicit val circeDecoder: Decoder[Success] = Decoder.forProduct1[Success, Option[List[Item]]]("items") {
      case Some(items) => apply(items)
      case None        => apply(List.empty)
    }
  }

  implicit def circeDecoder(component: Component.SoldOutResultsV2): Decoder[SoldOutResultsV2] = Decoder.instance[SoldOutResultsV2] { (c: HCursor) =>
    val circeDecoders = List[Decoder[SoldOutResultsV2]](
      Decoder[Failure].widen,
      Decoder[Success].widen
    ).reduceLeft(_ or _)

    c.downField(component.stateId).as[SoldOutResultsV2](circeDecoders)
  }

  implicit def jsonReader(component: Component.SoldOutResultsV2): JsonReader[SoldOutResultsV2] =
    JsonReader.builder
      .addField(
        component.stateId,
        JsonReader.builder
          .addField[Option[List[Item]]]("items")
          .addField[Option[String]]("error")
          .buildReader {
            case (Some(items), _) => Success(items)
            case (_, None)        => Success(List.empty)
            case (_, Some(error)) => Failure(error)
          }
      )
      .buildReader(identity)
}
