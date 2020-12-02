package marketplace.services

import cats.Monad
import cats.syntax.all._
import cats.effect.Resource
import tofu.data.derived.ContextEmbed
import tofu.streams.Evals
import tofu.syntax.streams.evals._
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import io.circe.{Decoder, Json}

trait Parse[S[_]] {
  def parse[R: Decoder]: S[Json] => S[Option[R]] // ToDo: use [[tofu.Raise]] instead of [[Option]]
}

object Parse extends ContextEmbed[Parse] {
  def apply[S[_]](implicit ev: Parse[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad, S[_]: Monad: Evals[*[_], F]](implicit logs: Logs[I, F]): Resource[I, Parse[S]] =
    Resource.liftF(logs.forService[Parse[F]].map(implicit l => new Impl[F, S]))

  private final class Impl[F[_]: Monad: Logging, S[_]: Monad: Evals[*[_], F]] extends Parse[S] {
    def parse[R](implicit decoder: Decoder[R]): S[Json] => S[Option[R]] = _.evalMap { json =>
      json.as[R] match {
        case Right(result) => (Some(result): Option[R]).pure[F]
        case Left(error)   =>
          error"Get ${error.toString()} during parsing JSON: ${json.toString()}" *>
            (None: Option[R]).pure[F]
      }
    }
  }
}
