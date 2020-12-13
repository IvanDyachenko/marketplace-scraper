package marketplace.services

import cats.Monad
import tofu.syntax.monadic._
import cats.effect.{Clock, Resource, Sync}
import supertagged.postfix._
import derevo.derive
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.generate.GenUUID
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import io.circe.Json

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.models.{CrawlerCommand, CrawlerEvent, EventId, HandleYandexMarketRequest, YandexMarketRequestHandled}
import marketplace.models.Timestamp

@derive(representableK)
trait Crawl[F[_]] {
  def handle(command: CrawlerCommand): F[CrawlerEvent]
}

object Crawl {

  private final class Logger[F[_]: Monad: Logging] extends Crawl[Mid[F, *]] {
    def handle(command: CrawlerCommand): Mid[F, CrawlerEvent] =
      info"Start handling ${command}" *> _
  }

  private final class Impl[F[_]: Sync: Clock: GenUUID](httpClient: HttpClient[F]) extends Crawl[F] {
    def handle(command: CrawlerCommand): F[CrawlerEvent] = command match {
      case HandleYandexMarketRequest(_, _, request) =>
        for {
          raw     <- httpClient.send[Json](request)
          uuid    <- GenUUID[F].randomUUID
          instant <- Clock[F].instantNow
        } yield YandexMarketRequestHandled(uuid @@ EventId, Timestamp(instant), raw)
    }
  }

  def apply[F[_]](implicit ev: Crawl[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Sync: Clock: GenUUID](httpClient: HttpClient[F])(implicit logs: Logs[I, F]): Resource[I, Crawl[F]] =
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
