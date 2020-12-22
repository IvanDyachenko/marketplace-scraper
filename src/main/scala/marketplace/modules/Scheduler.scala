package marketplace.modules

import cats.implicits._
import cats.{Foldable, Monad}
import cats.effect.{Resource, Timer}
import cats.tagless.syntax.functorK._
import tofu.syntax.embed._
import derevo.derive
import tofu.higherKind.derived.representableK
import fs2.Stream
import tofu.fs2.LiftStream

import marketplace.config.SchedulerConfig

@derive(representableK)
trait Scheduler[S[_]] {
  def run: S[Unit]
}

object Scheduler {

  def apply[F[_]](implicit ev: Scheduler[F]): ev.type = ev

  def make[I[_]: Monad: Timer, S[_]: LiftStream[*[_], I], O, G[_]: Foldable](
    f: => G[O]
  )(config: SchedulerConfig): Resource[I, Scheduler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Scheduler[Stream[I, *]] = new Scheduler[Stream[I, *]] {
            def run: Stream[I, Unit] =
              Stream.awakeEvery[I](config.timeout).zipRight(Stream.emits(f.toList)).as(())
          }

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
