package ecommerce.clients

import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK

import ecommerce.models.{Request, Response}

@derive(representableK)
trait EcommerceClient[F[_]] {
  def send(request: Request): F[Response]
}

object EcommerceClient extends ContextEmbed[EcommerceClient] {
  def make[I[_], F[_]]: I[EcommerceClient[F]] = ???
}
