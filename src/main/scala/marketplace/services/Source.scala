package marketplace.services

import cats.Monad
import cats.effect.{Resource, Timer}
import tofu.syntax.monadic._
import fs2.Stream
import marketplace.config.SourceConfig

trait Source[F[_], A] {
  def source: F[A]
}

object Source {

  def apply[F[_], A](implicit ev: Source[F, A]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Timer, A](config: SourceConfig)(fa: Stream[F, A]): Resource[I, Source[Stream[F, *], A]] =
    Resource.liftF {
      new Source[Stream[F, *], A] {
        def source: Stream[F, A] =
          Stream
            .awakeEvery[F](config.every)
            .zipRight(fa)
            .repeat
      }.pure[I]
    }
}
