package marketplace

import tofu.WithContext
import tofu.concurrent.ContextT

package object context {
  type CrawlerF[F[+_], +A] = ContextT[F, CrawlerContext, A]

  type HasLoggers[F[_]] = F WithContext Loggers[F]
}
