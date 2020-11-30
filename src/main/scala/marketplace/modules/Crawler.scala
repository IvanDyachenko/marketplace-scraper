package marketplace.modules

import cats.Monad
import cats.effect.{Concurrent, Resource}
import cats.tagless.FunctorK
import tofu.WithContext
import tofu.syntax.context._
import tofu.syntax.monadic._
import tofu.syntax.embed._
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream

import marketplace.config.CrawlerConfig
import marketplace.clients.models.HttpResponse
import marketplace.services.CrawlService

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler extends ContextEmbed[CrawlService] {

  def make[I[_]: Monad, F[_]: Monad: Concurrent, S[_]: Monad: LiftStream[*[_], F]: WithContext[*[_], CrawlerConfig]](
    crawlService: CrawlService[Stream[F, *]]
  ): Resource[I, Crawler[S]] =
    Resource.liftF {
      context[S]
        .map { conf =>
          val CrawlerConfig(maxOpen, maxConcurrent, prefetchNumber) = conf

          val impl = new Impl[F](crawlService, maxOpen, maxConcurrent, prefetchNumber)

          FunctorK[Crawler].mapK(impl)(LiftStream[S, F].liftF)
        }
        .embed
        .pure[I]
    }

  private final class Impl[F[_]: Monad: Concurrent](
    crawlService: CrawlService[Stream[F, *]],
    maxOpen: Int,
    maxConcurrent: Int,
    prefetchNumber: Int
  ) extends Crawler[Stream[F, *]] {

    def run: Stream[F, Unit] =
      crawlService.flow
        .prefetchN(prefetchNumber)
        .balanceAvailable
        .parEvalMapUnordered(maxConcurrent) { requests =>
          crawlService.crawl(jsonMeta.put, jsonDecoder)(requests).pure[F]
        }
        .parJoin(maxOpen)
        .evalMap(_ => ().pure[F])

    private implicit val jsonMeta    = HttpResponse.jsonDoobieMeta
    private implicit val jsonDecoder = HttpResponse.jsonCirceDecoder
  }
}
