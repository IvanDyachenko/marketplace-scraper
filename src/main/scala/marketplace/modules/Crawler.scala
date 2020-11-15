package marketplace.modules

import cats.Monad
import cats.effect.{Concurrent, Resource}
import cats.tagless.FunctorK
import tofu.syntax.monadic._
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream

import marketplace.context.HasConfig
import marketplace.services.CrawlService

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler extends ContextEmbed[CrawlService] {

  def make[I[_]: Monad, F[_]: Monad: Concurrent, S[_]: LiftStream[*[_], F]: HasConfig](
    crawlService: CrawlService[Stream[F, *]] // FixMe
  ): Resource[I, Crawler[S]] =
    Resource.liftF(FunctorK[Crawler].mapK(new Impl[F](crawlService))(LiftStream[S, F].liftF).pure[I])

  private final class Impl[F[_]: Monad: Concurrent](crawlService: CrawlService[Stream[F, *]], maxConcurrent: Int = 10)
      extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      crawlService.flow.balanceAvailable
        .parEvalMapUnordered(maxConcurrent)(crawlService.crawl(_).pure[F])
        .parJoinUnbounded
        .evalMap(_ => Monad[F].unit)
  }
}
