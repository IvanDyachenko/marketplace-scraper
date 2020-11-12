package marketplace.services

import cats.{FlatMap, Monad}
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.syntax.monadic._
import tofu.syntax.streams.evals._
import tofu.streams.Evals

import marketplace.models.{MarketplaceRequest, MarketplaceResponse}
import marketplace.clients.MarketplaceClient

trait CrawlService[S[_]] {
  def flow: S[MarketplaceRequest]
  def crawl: S[MarketplaceRequest] => S[MarketplaceResponse]
}

object CrawlService extends ContextEmbed[CrawlService] {

  def apply[F[_]](implicit ev: CrawlService[F]): ev.type = ev

  def make[I[_]: Monad, F[_], S[_]: Evals[*[_], F]](implicit client: MarketplaceClient[F]): I[CrawlService[S]] =
    Monad[I].pure(new Impl[F, S]: CrawlService[S])
//  Monad[I].pure(FunctorK[CrawlService].mapK(new Impl[F])(LiftStream[S, F].liftF))

  private final class Impl[F[_], S[_]: Evals[*[_], F]](implicit client: MarketplaceClient[F]) extends CrawlService[S] {

    def flow: S[MarketplaceRequest] = ???

    def crawl: S[MarketplaceRequest] => S[MarketplaceResponse] = _.evalMap(client.send(_))
  }

  implicit val embed: Embed[CrawlService] = new Embed[CrawlService] {
    override def embed[F[_]: FlatMap](ft: F[CrawlService[F]]): CrawlService[F] =
      new CrawlService[F] {

        def flow: F[MarketplaceRequest] = ft >>= (_.flow)

        def crawl: F[MarketplaceRequest] => F[MarketplaceResponse] = requests => ft >>= (_.crawl(requests))
      }
  }
}
