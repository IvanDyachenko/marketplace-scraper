package marketplace.services

import cats.Monad
import tofu.syntax.monadic._
import cats.effect.Resource
import tofu.data.derived.ContextEmbed
import derevo.derive
import tofu.higherKind.derived.representableK

import marketplace.models.Response
import marketplace.repositories.MarketplaceRepo

@derive(representableK)
trait MarketplaceService[F[_]] {
  def publish(resp: Response): F[Unit]
}

object MarketplaceService extends ContextEmbed[MarketplaceService] {
  def apply[F[_]](implicit ev: MarketplaceService[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad](repo: MarketplaceRepo[F]): Resource[I, MarketplaceService[F]] =
    new Impl[F](repo).pure[Resource[I, *]]

  final class Impl[F[_]: Monad](repo: MarketplaceRepo[F]) extends MarketplaceService[F] {
    def publish(resp: Response): F[Unit] = repo.add(resp)
  }
}
