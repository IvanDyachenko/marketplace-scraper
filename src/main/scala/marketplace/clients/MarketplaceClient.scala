package marketplace.clients

import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK

import marketplace.models.{Request, Response}

@derive(representableK)
trait MarketplaceClient[F[_]] {
  def send(request: Request): F[Response]
}

object MarketplaceClient extends ContextEmbed[MarketplaceClient] {
  def make[I[_], F[_]]: I[MarketplaceClient[F]] = ???
}
