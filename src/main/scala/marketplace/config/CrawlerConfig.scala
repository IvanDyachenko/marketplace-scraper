package marketplace.config

import cats.Monad
import tofu.syntax.monadic._
import tofu.optics.macros.{promote, ClassyOptics}

@ClassyOptics
case class HttpConfig(proxyHost: String, proxyPort: Int, maxConnections: Int, maxConnectionsPerHost: Int)

@ClassyOptics
case class ClickhouseConfig(url: String, user: String, pass: String)

@ClassyOptics
case class CrawlerConfig(@promote httpConfig: HttpConfig, @promote clickhouseConfig: ClickhouseConfig, maxConcurrent: Int)

object CrawlerConfig {

  def make[I[_]: Monad]: I[CrawlerConfig] =
    CrawlerConfig(
      HttpConfig("127.0.0.1", 8888, 200, 100),
      ClickhouseConfig("jdbc:clickhouse://localhost:8123/test", "default", ""),
      50
    ).pure[I]
}
