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
trait HandleCrawlerCommand[F[_]] {
  def handle(command: CrawlerCommand): F[Unit]
}

object HandleCrawlerCommand {

  private final class Logger[F[_]: Monad: Logging] extends HandleCrawlerCommand[Mid[F, *]] {
    def handle(command: CrawlerCommand): Mid[F, Unit] =
      _ *> info"Handle ${command}"
  }

  private final class Impl[F[_]: Monad: Sync](httpClient: HttpClient[F]) extends HandleCrawlerCommand[F] {
    def handle(command: CrawlerCommand): F[Unit] = command match {
      case HandleYandexMarketRequest(_, _, request) => httpClient.send[Json](request).map(_ => ())
    }
  }

  def apply[F[_]](implicit ev: HandleCrawlerCommand[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Sync](httpClient: HttpClient[F])(implicit logs: Logs[I, F]): Resource[I, HandleCrawlerCommand[F]] =
    Resource.liftF {
      logs
        .forService[HandleCrawlerCommand[F]]
        .map { implicit l =>
          val service = new Impl[F](httpClient)

          val logger: HandleCrawlerCommand[Mid[F, *]] = new Logger[F]

          logger.attach(service)
        }
    }

}
