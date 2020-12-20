package marketplace.modules

import cats.effect.Resource
import tofu.WithRun
import derevo.derive
import tofu.higherKind.derived.representableK

import marketplace.context.AppContext
import marketplace.services.Crawl

@derive(representableK)
trait Crawler[F[_]] {
  def run: F[Unit]
}

object Crawler {

  def apply[F[_]](implicit ev: Crawler[F]): ev.type = ev

  def make[I[_], F[_]: WithRun[*[_], I, AppContext]](crawl: Crawl[F]): Resource[I, Crawler[I]] = ???
}
