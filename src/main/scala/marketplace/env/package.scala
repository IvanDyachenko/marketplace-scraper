package marketplace

import cats.effect.ConcurrentEffect
import tofu.WithContext
import tofu.env.Env

import marketplace.config.Config

package object env {
  type App[+A] = Env[Environment, A]

  type HasCE[F[_]]     = F WithContext ConcurrentEffect[F]
  type HasConfig[F[_]] = F WithContext Config
}
