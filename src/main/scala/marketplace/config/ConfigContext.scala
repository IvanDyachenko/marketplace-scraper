package marketplace.config

import cats.effect.{Blocker, ContextShift, Sync}
import cats.syntax.apply._
import tofu.optics.macros.ClassyOptics

@ClassyOptics("contains_")
final case class ConfigContext(
  httpConfig: HttpConfig,
  clickhouseConfig: ClickhouseConfig,
  crawlerConfig: CrawlerConfig
)

object ConfigContext {
  def make[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[ConfigContext] =
    (HttpConfig.loadF[F], ClickhouseConfig.loadF[F], CrawlerConfig.loadF[F]).mapN(ConfigContext.apply)
}
