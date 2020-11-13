package marketplace

import tofu.WithContext
import tofu.concurrent.ContextT
import marketplace.config.CrawlerConfig

package object context {
  type CrawlerF[F[+_], +A] = ContextT[F, CrawlerContext, A]

  type HasConfig[F[_]]  = F WithContext CrawlerConfig
  type HasLoggers[F[_]] = F WithContext Loggers[F]
}
