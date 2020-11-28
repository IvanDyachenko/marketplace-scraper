package marketplace.models.yandex.market

import io.circe.Decoder
import io.circe.derivation.deriveDecoder
import derevo.derive
import tofu.logging.derivation.loggable

/** Информация о рейтинге.
  *
  * @param value Средняя оценка рейтинга.
  * @param count Кол-во оценок.
  */
@derive(loggable)
final case class Rating(value: Double, count: Int)

object Rating {
  implicit val circeDecoder: Decoder[Rating] = deriveDecoder
}
