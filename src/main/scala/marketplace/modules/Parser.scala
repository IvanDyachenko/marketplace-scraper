package marketplace.modules

import cats.Monad
import cats.effect.Resource
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream

import marketplace.services.Parse

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser extends ContextEmbed[Parser] {
  def apply[S[_]](implicit ev: Parser[S]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad, S[_]: LiftStream[*[_], F]](parse: Parse[Stream[F, *]]): Resource[I, Parser[S]] = ???
}
