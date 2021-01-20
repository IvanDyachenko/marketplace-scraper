package marketplace.services

import cats.implicits._
import tofu.syntax.handle._
import tofu.syntax.logging._
import derevo.derive
import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.Handle
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.generate.GenUUID
import tofu.logging.{Logging, Logs}
import io.circe.Json

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.clients.HttpClient.HttpClientError
import marketplace.models.crawler.{CrawlerEvent => Event, CrawlerCommand => Command}

@derive(representableK)
trait Crawl[F[_]] {
  def handle(command: Command): F[Option[Event]]
}

object Crawl {
  private final class Logger[F[_]: Monad: Logging] extends Crawl[Mid[F, *]] {
    def handle(command: Command): Mid[F, Option[Event]] =
      trace"Execution of the ${command} has started" *> _.flatTap {
        case None => error"Execution of the ${command} has been completed with an error"
        case _    => trace"Execution of the ${command} has been successfully completed"
      }
  }

  private final class Impl[F[_]: Monad: Clock: GenUUID: HttpClient: Handle[*[_], HttpClientError]] extends Crawl[F] {
    def handle(command: Command): F[Option[Event]] = command match {
      case Command.HandleOzonRequest(_, _, _, request) =>
        HttpClient[F].send[Json](request).attempt >>= (_.toOption.traverse(Event.ozonRequestHandled[F](request, _)))
    }
  }

  def apply[F[_]](implicit ev: Crawl[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Clock: GenUUID: HttpClient: Handle[*[_], HttpClientError]](implicit
    logs: Logs[I, F]
  ): Resource[I, Crawl[F]] =
    Resource.liftF {
      logs
        .forService[Crawl[F]]
        .map { implicit l =>
          val service = new Impl[F]

          val logger: Crawl[Mid[F, *]] = new Logger[F]

          logger.attach(service)
        }
    }
}
