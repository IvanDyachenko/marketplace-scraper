package marketplace.modules

import cats.Monad
import cats.effect.{Concurrent, Resource}
import cats.tagless.FunctorK
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import tofu.WithContext
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream
import io.circe.{Decoder, Json}

import marketplace.config.CrawlerConfig
import marketplace.services.Crawl

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler extends ContextEmbed[Crawl] {
  def apply[S[_]](implicit ev: Crawler[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Concurrent, S[_]: Monad: LiftStream[*[_], F]: WithContext[*[_], CrawlerConfig]](
    crawl: Crawl[Stream[F, *]]
  ): Resource[I, Crawler[S]] =
    Resource.liftF {
      context[S]
        .map { case CrawlerConfig(maxOpen, maxConcurrent, prefetchNumber) =>
          val impl = new Impl[F](crawl, maxOpen, maxConcurrent, prefetchNumber)
          FunctorK[Crawler].mapK(impl)(LiftStream[S, F].liftF)
        }
        .embed
        .pure[I]
    }

  private final class Impl[F[_]: Monad: Concurrent](
    crawl: Crawl[Stream[F, *]],
    maxOpen: Int,
    maxConcurrent: Int,
    prefetchNumber: Int
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      crawl.flow
        .prefetchN(prefetchNumber)
        .balanceAvailable
        .parEvalMapUnordered(maxConcurrent)(crawl.crawl[Json](Decoder.decodeJson)(_).pure[F])
        .parJoin(maxOpen)
        .evalMap(_ => ().pure[F])
  }
}
