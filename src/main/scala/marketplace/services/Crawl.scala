package marketplace.services

import cats.Monad
import cats.implicits._
import cats.effect.{Resource, Sync}
import derevo.derive
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import io.circe.Json

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.models.{CrawlerCommand, HandleYandexMarketRequest}

@derive(representableK)
trait Crawl[F[_]] {
  def handle(command: CrawlerCommand): F[Unit]
}

object Crawl {

  private final class Logger[F[_]: Monad: Logging] extends Crawl[Mid[F, *]] {
    def handle(command: CrawlerCommand): Mid[F, Unit] =
      _ *> info"Handle ${command}"
  }

  private final class Impl[F[_]: Monad: Sync](httpClient: HttpClient[F]) extends Crawl[F] {
    def handle(command: CrawlerCommand): F[Unit] = command match {
      case HandleYandexMarketRequest(_, _, request) => httpClient.send[Json](request).map(_ => ())
    }
  }

  def apply[F[_]](implicit ev: Crawl[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Sync](httpClient: HttpClient[F])(implicit logs: Logs[I, F]): Resource[I, Crawl[F]] =
    Resource.liftF {
      logs
        .forService[Crawl[F]]
        .map { implicit l =>
          val service = new Impl[F](httpClient)

          val logger: Crawl[Mid[F, *]] = new Logger[F]

          logger.attach(service)
        }
    }

}
