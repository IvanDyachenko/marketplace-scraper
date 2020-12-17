package marketplace.env

import cats.{Applicative, Defer}
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource}
import tofu.{Execute, WithLocal}
import tofu.lift.Unlift
import tofu.optics.Contains
import tofu.optics.macros.{promote, ClassyOptics}
import tofu.logging.{Loggable, LoggableContext, Logs}

import marketplace.config.Config
import marketplace.clients.HttpClient

@ClassyOptics
final case class Environment(
  @promote config: Config,
  httpClient: HttpClient[App]
)

object Environment {

  implicit val loggable: Loggable[Environment]           = Loggable.empty
  implicit val loggableContext: LoggableContext[App[+*]] = LoggableContext.of[App].instance[Environment]

  implicit def subContext[F[_]: Applicative: Defer, C](implicit
    lens: Environment Contains C,
    wl: WithLocal[F, Environment]
  ): F WithLocal C = WithLocal[F, Environment].subcontext(lens)

  def make[I[_]: Execute: ContextShift: ConcurrentEffect: Unlift[*[_], App]](implicit
    blocker: Blocker,
    logs: Logs[I, App]
  ): Resource[I, Environment] =
    for {
      config     <- Resource.liftF(Config.make[I])
      httpClient <- HttpClient.make[I, App](config.httpConfig)
    } yield Environment(config, httpClient)
}
