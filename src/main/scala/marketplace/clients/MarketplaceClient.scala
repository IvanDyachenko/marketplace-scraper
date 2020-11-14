package marketplace.clients

import cats.Monad
import cats.effect.{Resource, Sync}
import tofu.syntax.monadic._
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import org.http4s.client.Client
import beru4s.api.BeruClient

import marketplace.models.{MarketplaceRequest, MarketplaceResponse}
import marketplace.models.{BeruMarketplaceRequest, BeruMarketplaceResponse}

@derive(representableK)
trait MarketplaceClient[F[_]] {
  def send(request: MarketplaceRequest): F[MarketplaceResponse]
}

object MarketplaceClient extends ContextEmbed[MarketplaceClient] {

  def make[I[_]: Monad, F[_]: Sync](client: Client[F])(implicit logs: Logs[I, F]): Resource[I, MarketplaceClient[F]] =
    Resource.liftF(logs.forService[MarketplaceClient[F]].map(implicit l => new Impl[F](client)))

  private final class Impl[F[_]: Sync: Logging](client: Client[F]) extends MarketplaceClient[F] {

    private val beruClient: BeruClient[F] = BeruClient.fromHttp4sClient(client)

    def send(request: MarketplaceRequest): F[MarketplaceResponse] =
      request match {
        case BeruMarketplaceRequest(req) => beruClient.execute(req).map(BeruMarketplaceResponse(_))
      }
  }
}
