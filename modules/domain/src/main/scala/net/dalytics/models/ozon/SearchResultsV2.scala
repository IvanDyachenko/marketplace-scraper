package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import derevo.tethys.tethysReader
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}
import tethys.JsonReader

@derive(loggable)
sealed trait SearchResultsV2

object SearchResultsV2 {

  @derive(loggable, decoder, tethysReader)
  final case class Failure(error: String) extends SearchResultsV2

  @derive(loggable, decoder, tethysReader)
  final case class Success(items: List[Item]) extends SearchResultsV2

  private val circeDecoders: Decoder[SearchResultsV2] = List[Decoder[SearchResultsV2]](
    Decoder[Failure].widen,
    Decoder[Success].widen
  ).reduceLeft(_ or _)

  implicit def circeDecoder(component: Component.SearchResultsV2): Decoder[SearchResultsV2] = Decoder.instance[SearchResultsV2] { (c: HCursor) =>
    c.downField(component.stateId).as[SearchResultsV2](circeDecoders)
  }

  implicit def tethysJsonReader(component: Component.SearchResultsV2): JsonReader[SearchResultsV2] =
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
