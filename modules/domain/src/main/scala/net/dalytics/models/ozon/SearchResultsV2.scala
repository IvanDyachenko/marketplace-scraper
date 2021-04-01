package net.dalytics.models.ozon

import cats.implicits._
import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable
import io.circe.{Decoder, HCursor}

@derive(loggable)
sealed trait SearchResultsV2

object SearchResultsV2 {

  @derive(loggable, decoder)
  final case class Failure(error: String) extends SearchResultsV2

  @derive(loggable, decoder)
  final case class Success(items: List[Item]) extends SearchResultsV2

  implicit def circeDecoder(component: Component.SearchResultsV2): Decoder[SearchResultsV2] = Decoder.instance[SearchResultsV2] { (c: HCursor) =>
    val circeDecoders = List[Decoder[SearchResultsV2]](
      Decoder[Failure].widen,
      Decoder[Success].widen
    ).reduceLeft(_ or _)

    c.downField(component.stateId).as[SearchResultsV2](circeDecoders)
  }
}
