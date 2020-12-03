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
import io.circe.Json

import marketplace.config.CrawlerConfig
import marketplace.services.Crawl
import marketplace.clients.models.HttpResponse

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[HttpResponse[Json]]
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

    def run: Stream[F, HttpResponse[Json]] = ???
  }
}
