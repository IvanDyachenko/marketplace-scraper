package marketplace.models.yandex.market

import cats.Show
import cats.implicits._
import enumeratum.{CatsEnum, CirceEnum, Enum, EnumEntry, VulcanEnum}
import enumeratum.EnumEntry.Snakecase
import vulcan.generic._
import vulcan.{AvroNamespace, Codec}
import derevo.derive
import tofu.logging.LoggableEnum
import tofu.logging.derivation.loggable

@derive(loggable)
@AvroNamespace("yandex.market.models")
case class RearrFactor(name: RearrFactor.Name, value: Option[Int] = None)

object RearrFactor {

  implicit val show: Show[RearrFactor] = Show.show(_ match {
    case RearrFactor(name, Some(value)) => s"${name.show}=${value.show}"
    case RearrFactor(name, _)           => name.show
  })

  @AvroNamespace("yandex.market.models")
  sealed abstract class Name extends EnumEntry with Snakecase with Product with Serializable

  object Name extends Enum[Name] with CatsEnum[Name] with CirceEnum[Name] with LoggableEnum[Name] with VulcanEnum[Name] {

    case object CommonlyPurchasedOrdered            extends Name
    case object MarketRebranded                     extends Name
    case object MarketBlueSearchAuction             extends Name
    case object MarketBlueSubjectFederationDistrict extends Name
    case object MarketBlueSearchAuctionSupplierBid  extends Name
    case object MarketPromoBlueGenericBundle        extends Name
    case object MarketPromoBlueCheapestAsGift4      extends Name
    case object BuyerPriceNominalInclPromo          extends Name // FixMe: with Hyphencase

    val values = findValues

    implicit final class Ops(private val name: Name) extends AnyVal {
      def apply(): RearrFactor           = RearrFactor(name)
      def apply(value: Int): RearrFactor = RearrFactor(name, Some(value))
    }
  }

  implicit val vulcanCodec: Codec[RearrFactor] = Codec.derive[RearrFactor]
}
