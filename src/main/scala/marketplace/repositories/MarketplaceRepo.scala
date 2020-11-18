package marketplace.repositories

import cats.{Apply, Monad}
import tofu.syntax.monadic._
import cats.effect.Resource
import tofu.higherKind.Mid
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import doobie.ConnectionIO
import doobie.util.log.LogHandler
//import doobie.implicits._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

import marketplace.models.MarketplaceResponse

@derive(representableK)
trait MarketplaceRepo[DB[_]] {
  def add(response: MarketplaceResponse): DB[Unit]
}

object MarketplaceRepo extends ContextEmbed[MarketplaceRepo] {

  def make[I[_]: Monad, DB[_]: Monad: LiftConnectionIO](elh: EmbeddableLogHandler[DB])(implicit
    logs: Logs[I, DB]
  ): Resource[I, MarketplaceRepo[DB]] =
    Resource.liftF {
      logs
        .forService[MarketplaceRepo[DB]]
        .map(implicit l => new LoggingMid[DB].attach(elh.embedLift(implicit lh => new Impl)))
    }

  private final class Impl(implicit lh: LogHandler) extends MarketplaceRepo[ConnectionIO] {
    override def add(response: MarketplaceResponse): ConnectionIO[Unit] =
      ???
  }

  final class LoggingMid[DB[_]: Apply: Logging] extends MarketplaceRepo[Mid[DB, *]] {
    def add(response: MarketplaceResponse): Mid[DB, Unit] =
      info"Send response data to ClickHouse" *> _
  }
}
