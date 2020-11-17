package marketplace

import tofu.WithContext
import tofu.env.Env

import marketplace.config.CrawlerConfig

package object context {
  type CrawlerF[+A] = Env[CrawlerContext, A]

  type HasConfig[F[_]] = F WithContext CrawlerConfig
}
