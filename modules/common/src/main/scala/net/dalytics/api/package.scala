package net.dalytics

import cats.{~>, Functor}

package object api {
  trait BifunctorK[U[f[_], g[_]]] {
    def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: U[F, G])(fw: F ~> W)(gq: G ~> Q): U[W, Q]
  }
}
