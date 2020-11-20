package marketplace.models.yandex.market

import cats.Show
import cats.implicits._
import tofu.logging.Loggable

final case class ApiVersion private[ApiVersion] (major: Int, minor: Int, patch: Int) extends Ordered[ApiVersion] {
  override def toString: String      = s"v${this.major}.${this.minor}.${this.patch}"
  def compare(that: ApiVersion): Int = (this.major, this.minor, this.patch).compare((that.major, that.minor, that.patch))
}

object ApiVersion {
  val `2.1.6` = new ApiVersion(2, 1, 6)

  implicit val show: Show[ApiVersion]         = Show.fromToString
  implicit val loggable: Loggable[ApiVersion] = Loggable.stringValue.contramap(_.show)
}
