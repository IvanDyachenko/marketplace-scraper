package marketplace

import cats.effect.ConcurrentEffect
import tofu.env.Env
import tofu.WithContext

import marketplace.config.Config

package object context {
  type AppF[+A] = Env[AppContext, A]

  type HasCE[F[_]]     = F WithContext ConcurrentEffect[F]
  type HasConfig[F[_]] = F WithContext Config
}
