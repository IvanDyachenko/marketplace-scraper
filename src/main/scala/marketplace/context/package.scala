package marketplace

import tofu.WithContext
import tofu.env.Env

import marketplace.config.ConfigContext

package object context {
  type CrawlerF[+A] = Env[AppContext, A]

  type HasConfig[F[_]] = F WithContext ConfigContext
}
