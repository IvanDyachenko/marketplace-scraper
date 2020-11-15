package marketplace.services

import cats.{FlatMap, Monad}
import cats.effect.Resource
import tofu.syntax.context._
import tofu.syntax.monadic._
import tofu.syntax.embed._
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.streams.{Emits, Evals}
import tofu.syntax.streams.evals._

import marketplace.syntax._
import marketplace.context.HasConfig
import marketplace.clients.MarketplaceClient
import marketplace.models.{MarketplaceRequest, MarketplaceResponse}

trait CrawlService[S[_]] {
  def flow: S[MarketplaceRequest]
  def crawl: S[MarketplaceRequest] => S[MarketplaceResponse]
}

object CrawlService extends ContextEmbed[CrawlService] {

  def apply[F[_]](implicit ev: CrawlService[F]): ev.type = ev

  def make[I[_]: Monad, F[_], S[_]: Monad: Evals[*[_], F]: HasConfig](client: MarketplaceClient[F]): Resource[I, CrawlService[S]] =
    Resource.liftF(context[S].map(conf => new Impl[F, S](client): CrawlService[S]).embed.pure[I])

  private final class Impl[F[_], S[_]: Emits: Evals[*[_], F]](client: MarketplaceClient[F]) extends CrawlService[S] {

    implicit val marketplaceClient: MarketplaceClient[F] = client

    def flow: S[MarketplaceRequest] = ???

    def crawl: S[MarketplaceRequest] => S[MarketplaceResponse] = _.evalMap(_.call)
  }

  implicit val embed: Embed[CrawlService] = new Embed[CrawlService] {
    override def embed[F[_]: FlatMap](ft: F[CrawlService[F]]): CrawlService[F] = new CrawlService[F] {
      def flow: F[MarketplaceRequest]                            = ft >>= (_.flow)
      def crawl: F[MarketplaceRequest] => F[MarketplaceResponse] = requests => ft >>= (_.crawl(requests))
    }
  }
}
