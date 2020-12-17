package marketplace

import tofu.WithContext
import tofu.env.Env

import marketplace.config.Config

package object context {
  type AppF[+A] = Env[AppContext, A]

  type HasConfig[F[_]] = F WithContext Config
}
