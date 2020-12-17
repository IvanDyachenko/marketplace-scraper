package marketplace

import tofu.WithContext
import tofu.env.Env

import marketplace.config.Config

package object env {
  type App[+A] = Env[Environment, A]

  type HasConfig[F[_]] = F WithContext Config
}
