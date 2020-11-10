package ecommerce.modules

import cats.Monad
import derevo.derive
import tofu.higherKind.derived.embed
import tofu.streams.{Broadcast, Evals}
import tofu.syntax.streams.evals._
import tofu.syntax.streams.broadcast._

import ecommerce.services.CrawlService
import ecommerce.services.EcommerceService

@derive(embed)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  def make[I[_]: Monad, F[_], S[_]: Evals[*[_], F]](implicit
    crawlService: CrawlService[S],
    ecommerceService: EcommerceService[S]
  ): I[Crawler[S]] = ???

  private final class Impl[F[_]: Monad, S[_]: Broadcast: Evals[*[_], F]](implicit
    crawlService: CrawlService[S],
    ecommerceService: EcommerceService[S]
  ) extends Crawler[S] {
    def run: S[Unit] =
      ecommerceService.requestStream
        .broadcastThrough(10)(crawlService.crawl)
        .evalMap(_ => Monad[F].unit)
  }
}
