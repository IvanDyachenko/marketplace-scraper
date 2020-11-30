package marketplace.services

import cats.{FlatMap, Monad}
import tofu.syntax.monadic._
import cats.effect.Resource
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.syntax.embed._
import tofu.syntax.context._
import tofu.streams.{Emits, Evals}
import tofu.syntax.streams.evals._
import io.circe.Decoder
import doobie.util.Put

import marketplace.context.HasConfig
import marketplace.clients.HttpClient
import marketplace.clients.models.HttpResponse

import marketplace.models.{Request => HttpRequest}
import marketplace.repositories.CrawlRepo

trait CrawlService[S[_]] {
  def flow: S[HttpRequest]
  def crawl[R: Put: Decoder]: S[HttpRequest] => S[HttpResponse[R]]
}

object CrawlService extends ContextEmbed[CrawlService] {
  def apply[S[_]](implicit ev: CrawlService[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad, S[_]: Monad: Evals[*[_], F]: HasConfig](
    httpClient: HttpClient[F],
    crawlRepo: CrawlRepo[F]
  ): Resource[I, CrawlService[S]] =
    Resource.liftF(context[S].map(conf => new Impl[F, S](httpClient, crawlRepo): CrawlService[S]).embed.pure[I])

  private final class Impl[F[_]: Monad, S[_]: Monad: Emits: Evals[*[_], F]](httpClient: HttpClient[F], crawlRepo: CrawlRepo[F])
      extends CrawlService[S] {

    def flow: S[HttpRequest] = ???

    def crawl[R: Put: Decoder]: S[HttpRequest] => S[HttpResponse[R]] =
      _.evalMap(req => httpClient.send[R](req))
//    _.evalMap(req => httpClient.send[R](req).flatTap(resp => crawlRepo.add(req, resp)))
  }

  implicit val embed: Embed[CrawlService] = new Embed[CrawlService] {
    override def embed[F[_]: FlatMap](ft: F[CrawlService[F]]): CrawlService[F] = new CrawlService[F] {
      def flow: F[HttpRequest] = ft >>= (_.flow)

      def crawl[R](implicit put: Put[R], decoder: Decoder[R]): F[HttpRequest] => F[HttpResponse[R]] =
        requests => ft >>= (_.crawl(put, decoder)(requests))
    }
  }
}
