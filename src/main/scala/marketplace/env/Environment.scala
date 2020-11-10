package marketplace.env

import tofu.WithLocal
import tofu.optics.Contains
import tofu.optics.macros.ClassyOptics

import marketplace.clients.MarketplaceClient

@ClassyOptics
final case class Environment[F[_]](
  marketplaceClient: MarketplaceClient[F]
)

object Environment {

  implicit def appSubContext[F[_], C](implicit
    e: Environment[F] Contains C,
    wl: WithLocal[F, Environment[F]]
  ): F WithLocal C =
    WithLocal[F, Environment[F]].subcontext(e)
}
