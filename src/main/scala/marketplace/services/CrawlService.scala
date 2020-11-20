package marketplace.services

import cats.{FlatMap, Monad}
import tofu.syntax.monadic._
import cats.effect.Resource
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.syntax.embed._
import tofu.syntax.context._
import tofu.streams.{Emits, Evals}
//import tofu.syntax.streams.emits._
import tofu.syntax.streams.evals._

import marketplace.context.HasConfig
import marketplace.clients.HttpClient
import marketplace.models.{Request, Response}

trait CrawlService[S[_]] {
  def flow: S[Request]
  def crawl: S[Request] => S[Response]
}

object CrawlService extends ContextEmbed[CrawlService] {
  def apply[S[_]](implicit ev: CrawlService[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad, S[_]: Monad: Evals[*[_], F]: HasConfig](httpClient: HttpClient[F]): Resource[I, CrawlService[S]] =
    Resource.liftF(context[S].map(conf => new Impl[F, S](httpClient): CrawlService[S]).embed.pure[I])

  private final class Impl[F[_]: Monad, S[_]: Monad: Emits: Evals[*[_], F]](httpClient: HttpClient[F]) extends CrawlService[S] {

    def flow: S[Request] = ???

    def crawl: S[Request] => S[Response] =
      _.evalMap(req => httpClient.send(req))
  }

  implicit val embed: Embed[CrawlService] = new Embed[CrawlService] {
    override def embed[F[_]: FlatMap](ft: F[CrawlService[F]]): CrawlService[F] = new CrawlService[F] {
      def flow: F[Request]                 = ft >>= (_.flow)
      def crawl: F[Request] => F[Response] = requests => ft >>= (_.crawl(requests))
    }
  }
}
