package marketplace.clients

import cats.Monad
import tofu.syntax.monadic._
import cats.effect.Sync
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import tofu.logging.Logging

import marketplace.models.{MarketplaceRequest, MarketplaceResponse}

@derive(representableK)
trait MarketplaceClient[F[_]] {
  def send(request: MarketplaceRequest): F[MarketplaceResponse]
}

object MarketplaceClient extends ContextEmbed[MarketplaceClient] {

  def make[I[_]: Monad, F[_]: Sync: Logging]: I[MarketplaceClient[F]] =
    new MarketplaceClient[F] {
      override def send(request: MarketplaceRequest): F[MarketplaceResponse] = ???
    }.pure[I]
}
