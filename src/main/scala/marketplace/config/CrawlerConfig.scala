package marketplace.config

import cats.Monad
import tofu.syntax.monadic._
import tofu.optics.macros.ClassyOptics

case class HttpConfig(proxyHost: String, proxyPort: Int)

@ClassyOptics
case class CrawlerConfig(httpConfig: HttpConfig)

object CrawlerConfig {

  def make[I[_]: Monad]: I[CrawlerConfig] =
    CrawlerConfig(HttpConfig("127.0.0.1", 8888)).pure[I]
}
