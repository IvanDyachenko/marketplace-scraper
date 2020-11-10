package marketplace.services

import cats.{FlatMap, Monad}
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.syntax.monadic._
import tofu.syntax.streams.evals._
import tofu.streams.Evals

import marketplace.models.{Request, Response}
import marketplace.clients.MarketplaceClient

trait CrawlService[S[_]] {
  def flow: S[Request]
  def crawl: S[Request] => S[Response]
}

object CrawlService extends ContextEmbed[CrawlService] {

  def make[I[_]: Monad, F[_], S[_]: Evals[*[_], F]](implicit client: MarketplaceClient[F]): I[CrawlService[S]] =
    Monad[I].pure(new Impl[F, S]: CrawlService[S])
//  Monad[I].pure(FunctorK[CrawlService].mapK(new Impl[F])(LiftStream[S, F].liftF))

  private final class Impl[F[_], S[_]: Evals[*[_], F]](implicit client: MarketplaceClient[F]) extends CrawlService[S] {

    def flow: S[Request] = ???

    def crawl: S[Request] => S[Response] = _.evalMap(client.send(_))
  }

  implicit val embed: Embed[CrawlService] = new Embed[CrawlService] {
    override def embed[F[_]: FlatMap](ft: F[CrawlService[F]]): CrawlService[F] =
      new CrawlService[F] {

        def flow: F[Request] = ft >>= (_.flow)

        def crawl: F[Request] => F[Response] = requests => ft >>= (_.crawl(requests))
      }
  }
}
