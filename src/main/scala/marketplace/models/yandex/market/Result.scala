package marketplace.models.yandex.market

import cats.implicits._
import io.circe.Decoder
import derevo.derive
import derevo.circe.decoder
import tofu.logging.LoggableEnum
import tofu.logging.derivation.{loggable, masked, MaskMode}
import enumeratum.{CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Uppercase

@derive(loggable)
sealed trait Result {
  def status: Result.Status
}

@derive(loggable, decoder)
final case class FailureResult(
  @masked(MaskMode.Erase) errors: List[ErrorDescr]
) extends Result {
  def status: Result.Status = Result.Status.Error
}

@derive(loggable, decoder)
final case class ModelsResult(
  @masked(MaskMode.Erase) context: Context,
  @masked(MaskMode.Erase) items: List[Model]
) extends Result {
  val status: Result.Status = Result.Status.Ok
}

object Result {
  sealed trait Status extends EnumEntry with Uppercase

  object Status extends Enum[Status] with CirceEnum[Status] with LoggableEnum[Status] with VulcanEnum[Status] {

    case object Ok    extends Status
    case object Error extends Status

    val values = findValues
  }

  implicit val circeDecoder: Decoder[Result] =
    List[Decoder[Result]](
      Decoder[FailureResult].widen,
      Decoder[ModelsResult].widen
    ).reduceLeft(_ or _)

//implicit val avroCodec: Codec[Result] =
//  Codec.union[Result](alt => alt[FailureResult] |+| alt[ModelsResult])
}

object FailureResult {
//implicit val avroCodec: Codec[FailureResult] =
//  Codec.record[FailureResult]("FailureResult", "yandex.market.models")(field =>
//    field("status", _.status) *> (field("errors", _.errors)).map(apply)
//  )
}

object ModelsResult {
//implicit val avroCodec: Codec[ModelsResult] =
//  Codec.record[ModelsResult]("ModelsResult", "yandex.market.models")(field =>
//    field("status", _.status) *> (field("context", _.context), field("items", _.items)).mapN(apply)
//  )
}
