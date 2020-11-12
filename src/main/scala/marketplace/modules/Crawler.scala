package marketplace.modules

import cats.Monad
import tofu.syntax.monadic._
import derevo.derive
import tofu.higherKind.derived.embed
import tofu.streams.{Broadcast, Evals}
import tofu.syntax.streams.evals._
import tofu.syntax.streams.broadcast._

import marketplace.services.CrawlService

@derive(embed)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  def make[I[_]: Monad, F[_]: Monad, S[_]: Broadcast: Evals[*[_], F]](implicit
    crawlService: CrawlService[S]
  ): I[Crawler[S]] =
    (new Impl[F, S]: Crawler[S]).pure[I]

  private final class Impl[F[_]: Monad, S[_]: Broadcast: Evals[*[_], F]](implicit crawlService: CrawlService[S])
      extends Crawler[S] {

    def run: S[Unit] =
      crawlService.flow
        .broadcastThrough(10)(crawlService.crawl)
        .evalMap(_ => Monad[F].unit)
  }
}
