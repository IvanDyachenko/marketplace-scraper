package marketplace.modules

import cats.Monad
import cats.effect.{Concurrent, Resource}
import cats.tagless.FunctorK
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import tofu.WithContext
import derevo.derive
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream
import io.circe.Json

import marketplace.config.ParserConfig
import marketplace.services.Parse
import marketplace.models.yandex.market.Result

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {

  private final class Impl[F[_]: Monad: Concurrent](flow: Stream[F, Json], parse: Parse[Stream[F, *]], maxOpen: Int, maxConcurrent: Int)
      extends Parser[Stream[F, *]] {

    def run: Stream[F, Unit] =
      flow.balanceAvailable
        .parEvalMapUnordered(maxConcurrent)(parse.parse[Result](Result.circeDecoder)(_).pure[F])
        .parJoin(maxOpen)
        .evalMap(_ => ().pure[F])
  }

  def apply[S[_]](implicit ev: Parser[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Concurrent, S[_]: Monad: LiftStream[*[_], F]: WithContext[*[_], ParserConfig]](
    flow: Stream[F, Json],
    parse: Parse[Stream[F, *]]
  ): Resource[I, Parser[S]] =
    Resource.liftF {
      context[S]
        .map { case ParserConfig(maxOpen, maxConcurrent) =>
          val impl = new Impl[F](flow, parse, maxOpen, maxConcurrent)
          FunctorK[Parser].mapK(impl)(LiftStream[S, F].liftF)
        }
        .embed
        .pure[I]
    }
}
