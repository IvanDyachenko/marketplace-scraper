package marketplace.modules

import cats.Monad
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import derevo.derive
import tofu.higherKind.derived.embed
import tofu.streams.{Broadcast, Evals}
import tofu.syntax.streams.evals._
import tofu.syntax.streams.broadcast._

import marketplace.context.HasConfig
import marketplace.services.CrawlService

@derive(embed)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  def make[I[_]: Monad, F[_]: Monad, S[_]: Monad: Broadcast: Evals[*[_], F]: HasConfig](implicit
    crawlService: CrawlService[S]
  ): I[Crawler[S]] =
    context[S].map(config => new Impl[F, S](config.broadcast): Crawler[S]).embed.pure[I]

  private final class Impl[F[_]: Monad, S[_]: Broadcast: Evals[*[_], F]](maxConcurrent: Int)(implicit
    crawlService: CrawlService[S]
  ) extends Crawler[S] {

    def run: S[Unit] =
      crawlService.flow
        .broadcastThrough(maxConcurrent)(crawlService.crawl)
        .evalMap(_ => Monad[F].unit)
  }
}
