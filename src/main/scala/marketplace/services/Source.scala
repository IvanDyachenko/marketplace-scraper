package marketplace.services

import cats.Monad
import cats.effect.Timer
import fs2.Stream
import marketplace.config.SourceConfig

trait Source[F[_], A] {
  def source: F[A]
}

object Source {

  def apply[F[_], A](implicit ev: Source[F, A]): ev.type = ev

  def make[F[_]: Monad: Timer, A](config: SourceConfig)(fa: Stream[F, A]): Source[Stream[F, *], A] =
    new Source[Stream[F, *], A] {
      def source: Stream[F, A] =
        Stream
          .awakeEvery[F](config.every)
          .zipRight(fa)
          .repeat
    }
}
