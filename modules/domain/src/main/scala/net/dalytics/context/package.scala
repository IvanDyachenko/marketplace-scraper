package net.dalytics

import tofu.env.Env
import tofu.WithContext
import cats.effect.ConcurrentEffect

package object context {
  type AppF[+A] = Env[AppContext, A]

  type HasCE[F[_]] = F WithContext ConcurrentEffect[F]
//type HasConfig[F[_]] = F WithContext Config
}
