package net.dalytics.models.yandex.market

import derevo.derive
import derevo.circe.decoder
import tofu.logging.derivation.loggable

/** Информация о рейтинге.
  *
  * @param value Средняя оценка рейтинга.
  * @param count Кол-во оценок.
  */
@derive(loggable, decoder)
final case class Rating(value: Double, count: Int)
