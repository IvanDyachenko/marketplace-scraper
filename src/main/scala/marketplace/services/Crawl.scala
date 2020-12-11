package marketplace.services

import cats.{Applicative, FlatMap, Monad}
import tofu.syntax.monadic._
import cats.effect.Resource
import tofu.higherKind.Embed
import tofu.syntax.embed._
import tofu.syntax.context._
import tofu.streams.Evals
import tofu.syntax.streams.evals._
import io.circe.{Decoder, Encoder}

import marketplace.context.HasConfig
import marketplace.clients.HttpClient
import marketplace.clients.models.{HttpRequest, HttpResponse}

trait Crawl[S[_]] {
  def crawl[Req: Encoder, Res: Decoder]: S[HttpRequest[Req]] => S[HttpResponse[Res]]
}

object Crawl {

  private final class Impl[F[_], S[_]: Evals[*[_], F]](httpClient: HttpClient[F]) extends Crawl[S] {
    def crawl[Req: Encoder, Res: Decoder]: S[HttpRequest[Req]] => S[HttpResponse[Res]] =
      _.evalMap(req => httpClient.send[Req, Res](req))
  }

  def apply[S[_]](implicit ev: Crawl[S]): ev.type = ev

  def make[I[_]: Applicative, F[_], S[_]: Monad: Evals[*[_], F]: HasConfig](httpClient: HttpClient[F]): Resource[I, Crawl[S]] =
    Resource.liftF(context[S].map(conf => new Impl[F, S](httpClient): Crawl[S]).embed.pure[I])

  implicit val embed: Embed[Crawl] = new Embed[Crawl] {
    def embed[F[_]: FlatMap](ft: F[Crawl[F]]): Crawl[F] = new Crawl[F] {
      def crawl[Req, Res](implicit encoder: Encoder[Req], decoder: Decoder[Res]): F[HttpRequest[Req]] => F[HttpResponse[Res]] =
        requests => ft >>= (_.crawl(encoder, decoder)(requests))
    }
  }
}
