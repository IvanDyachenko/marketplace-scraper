package marketplace.modules

import cats.Monad
import cats.effect.{Concurrent, Resource}
import cats.tagless.FunctorK
import tofu.syntax.context._
import tofu.syntax.monadic._
import tofu.syntax.embed._
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream

import marketplace.context.HasConfig
import marketplace.services.{CrawlService, MarketplaceService}

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler extends ContextEmbed[CrawlService] {

  def make[I[_]: Monad, F[_]: Monad: Concurrent, S[_]: Monad: LiftStream[*[_], F]: HasConfig](
    crawlService: CrawlService[Stream[F, *]],
    marketplaceService: MarketplaceService[F]
  ): Resource[I, Crawler[S]] =
    Resource.liftF {
      context[S]
        .map(conf => FunctorK[Crawler].mapK(new Impl[F](crawlService, marketplaceService, conf.maxConcurrent))(LiftStream[S, F].liftF))
        .embed
        .pure[I]
    }

  private final class Impl[F[_]: Monad: Concurrent](
    crawlService: CrawlService[Stream[F, *]],
    marketplaceService: MarketplaceService[F],
    maxConcurrent: Int
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      crawlService.flow.balanceAvailable
        .parEvalMapUnordered(maxConcurrent)(crawlService.crawl(_).pure[F])
        .parJoinUnbounded
        .evalMap(_ => Monad[F].unit)
  }
}
