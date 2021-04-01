package net.dalytics.services

import cats.implicits._
import tofu.syntax.handle._
import tofu.syntax.logging._
import derevo.derive
import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import io.circe.Json

import net.dalytics.marshalling._
import net.dalytics.clients.{HttpClient, HttpClientError}
import net.dalytics.models.handler.{HandlerEvent => Event, HandlerCommand => Command}

@derive(representableK)
trait Handle[F[_]] {
  def handle(command: Command): F[Handle.Result]
}

object Handle {
  def apply[F[_]](implicit ev: Handle[F]): ev.type = ev

  final type Result = Either[HttpClientError, Event]

  private final class Logger[F[_]: Monad: Logging] extends Handle[Mid[F, *]] {
    def handle(command: Command): Mid[F, Result] =
      trace"Execution of the ${command} has started" *> _ flatTap {
        case Left(error)  => error"Execution of the ${command} has been completed with the ${error}"
        case Right(event) => trace"${event} has been successfully created as a result of execution of the ${command}"
      }
  }

  private final class Impl[F[_]: Monad: Clock: HttpClient: HttpClient.Handling] extends Handle[F] {
    def handle(command: Command): F[Result] = command match {
      case Command.HandleOzonRequest(_, request) => HttpClient[F].send[Json](request).attempt >>= (_.traverse(Event.ozonRequestHandled[F]))
    }
  }

  def make[
    I[_]: Monad,
    F[_]: Monad: Clock: HttpClient: HttpClient.Handling
  ](implicit logs: Logs[I, F]): Resource[I, Handle[F]] =
    Resource.eval {
      logs
        .forService[Handle[F]]
        .map { implicit l =>
          val service = new Impl[F]

          val logger: Handle[Mid[F, *]] = new Logger[F]

          logger.attach(service)
        }
    }
}
