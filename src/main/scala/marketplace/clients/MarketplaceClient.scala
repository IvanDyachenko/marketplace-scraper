package marketplace.clients

import cats.Monad
import cats.effect.{Resource, Sync}
import tofu.syntax.monadic._
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import org.http4s.client.Client

import marketplace.context.HasLoggers
import marketplace.models.{MarketplaceRequest, MarketplaceResponse}

@derive(representableK)
trait MarketplaceClient[F[_]] {
  def send(request: MarketplaceRequest): F[MarketplaceResponse]
}

object MarketplaceClient extends ContextEmbed[MarketplaceClient] {

  def make[I[_]: Monad, F[_]: Sync: HasLoggers](implicit client: Client[F]): Resource[I, MarketplaceClient[F]] =
    new Impl[F].pure[Resource[I, *]]

  private final class Impl[F[_]](implicit client: Client[F]) extends MarketplaceClient[F] {
    def send(request: MarketplaceRequest): F[MarketplaceResponse] = ???
  }
}
