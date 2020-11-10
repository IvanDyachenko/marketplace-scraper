package ecommerce.services

import cats.tagless.FunctorK
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream

import ecommerce.models.Request
import cats.Monad

@derive(representableK)
trait EcommerceService[S[_]] {
  def requestStream: S[Request]
}

object EcommerceService extends ContextEmbed[EcommerceService] {

  def make[I[_]: Monad, F[_], S[_]: LiftStream[*[_], F]]: I[EcommerceService[S]] =
    Monad[I].pure(FunctorK[EcommerceService].mapK(new Impl[F])(LiftStream[S, F].liftF))

  private final class Impl[F[_]] extends EcommerceService[Stream[F, *]] {
    def requestStream: Stream[F, Request] = ???
  }
}
