package marketplace.services

import cats.{FlatMap, Monad}
import tofu.syntax.monadic._
import cats.effect.{Clock, Resource}
import derevo.derive
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.generate.GenUUID
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import io.circe.Json

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.models.crawler.{Command, Event, HandleYandexMarketRequest}

@derive(representableK)
trait Crawl[F[_]] {
  def handle(command: Command): F[Event]
}

object Crawl {

  private final class Logger[F[_]: Monad: Logging] extends Crawl[Mid[F, *]] {
    def handle(command: Command): Mid[F, Event] =
      info"Start handling ${command}" *> _
  }

  private final class Impl[F[_]: FlatMap: Clock: GenUUID: HttpClient] extends Crawl[F] {
    def handle(command: Command): F[Event] = command match {
      case HandleYandexMarketRequest(_, _, _, request) =>
        HttpClient[F].send[Json](request) >>= (raw => Event.yandexMarketRequestHandled[F](request, raw))
    }
  }

  def apply[F[_]](implicit ev: Crawl[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Clock: GenUUID: HttpClient](implicit logs: Logs[I, F]): Resource[I, Crawl[F]] =
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
