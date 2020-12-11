package marketplace.models.yandex.market

import cats.syntax.functor._
import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Uppercase
import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.LoggableEnum
import tofu.logging.derivation.{loggable, masked, MaskMode}

@derive(loggable)
sealed trait Result {
  def status: Result.Status
}

@derive(loggable)
final case class FailureResult(
  @masked(MaskMode.Erase) errors: List[ErrorDescr]
) extends Result {
  def status: Result.Status = Result.Status.Error
}

@derive(loggable)
final case class ModelsResult(
  @masked(MaskMode.Erase) context: Context,
  @masked(MaskMode.Erase) items: List[Model]
) extends Result {
  val status: Result.Status = Result.Status.Ok
}

object Result {

  sealed trait Status extends EnumEntry with Uppercase

  object Status extends Enum[Status] with CirceEnum[Status] with LoggableEnum[Status] {

    case object Ok    extends Status
    case object Error extends Status

    val values = findValues
  }

  implicit val circeDecoderFailureResult: Decoder[FailureResult] = deriveDecoder
  implicit val circeDecoderModelsResult: Decoder[ModelsResult]   = deriveDecoder

  implicit val circeDecoder: Decoder[Result] =
    List[Decoder[Result]](
      Decoder[FailureResult].widen,
      Decoder[ModelsResult].widen
    ).reduceLeft(_ or _)
}
