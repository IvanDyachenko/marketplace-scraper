package marketplace.services

import cats.{FlatMap, Monad}
import tofu.syntax.monadic._
import cats.effect.Resource
import tofu.higherKind.Embed
import tofu.data.derived.ContextEmbed
import tofu.syntax.embed._
import tofu.syntax.context._
import tofu.streams.Evals
import tofu.syntax.streams.evals._
import io.circe.Decoder

import marketplace.context.HasConfig
import marketplace.clients.HttpClient
import marketplace.clients.models.HttpResponse

import marketplace.models.{Request => HttpRequest}

trait Crawl[S[_]] {
  def flow: S[HttpRequest]
  def crawl[R: Decoder]: S[HttpRequest] => S[HttpResponse[R]]
}

object Crawl extends ContextEmbed[Crawl] {
  def apply[S[_]](implicit ev: Crawl[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad, S[_]: Monad: Evals[*[_], F]: HasConfig](httpClient: HttpClient[F]): Resource[I, Crawl[S]] =
    Resource.liftF(context[S].map(conf => new Impl[F, S](httpClient): Crawl[S]).embed.pure[I])

  private final class Impl[F[_]: Monad, S[_]: Monad: Evals[*[_], F]](httpClient: HttpClient[F]) extends Crawl[S] {
    def flow: S[HttpRequest] =
      ???

    def crawl[R: Decoder]: S[HttpRequest] => S[HttpResponse[R]] =
      _.evalMap(req => httpClient.send[R](req))
  }

  implicit val embed: Embed[Crawl] = new Embed[Crawl] {
    def embed[F[_]: FlatMap](ft: F[Crawl[F]]): Crawl[F] = new Crawl[F] {
      def flow: F[HttpRequest] =
        ft >>= (_.flow)

      def crawl[R](implicit decoder: Decoder[R]): F[HttpRequest] => F[HttpResponse[R]] =
        requests => ft >>= (_.crawl(decoder)(requests))
    }
  }
}
