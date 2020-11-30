package marketplace.repositories

import cats.{Apply, Monad}
import cats.implicits._
import cats.effect.Resource
import tofu.higherKind.Mid
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import doobie.implicits._
import doobie.ConnectionIO
import doobie.util.Put
import doobie.util.log.LogHandler
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

import marketplace.clients.models.HttpResponse
import marketplace.models.{Request => HttpRequest}

@derive(representableK)
trait CrawlRepo[DB[_]] {
  def add[R: Put](request: HttpRequest, response: HttpResponse[R]): DB[Unit]
}

object CrawlRepo extends ContextEmbed[CrawlRepo] {
  def apply[F[_]](implicit ev: CrawlRepo[F]): ev.type = ev

  def make[I[_]: Monad, DB[_]: Monad: LiftConnectionIO](elh: EmbeddableLogHandler[DB])(implicit
    logs: Logs[I, DB]
  ): Resource[I, CrawlRepo[DB]] =
    Resource.liftF {
      logs
        .forService[CrawlRepo[DB]]
        .map(implicit l => new LoggingMid[DB].attach(elh.embedLift(implicit lh => new Impl)))
    }

  private final class Impl(implicit lh: LogHandler) extends CrawlRepo[ConnectionIO] {
    def add[R: Put](request: HttpRequest, response: HttpResponse[R]): ConnectionIO[Unit] =
      sql"""
           |INSERT INTO yandex.market_category_models
           |(uri, host, path, body_text)
           |VALUES
           |(
           |    ${request.uri.toString},
           |    ${request.host.toString},
           |    ${request.path.toString},
           |    ${response.result}
           |)
         """.stripMargin.update.run.void
  }

  final class LoggingMid[DB[_]: Apply: Logging] extends CrawlRepo[Mid[DB, *]] {
    def add[R: Put](request: HttpRequest, response: HttpResponse[R]): Mid[DB, Unit] =
      trace"Send data to ClickHouse" *> _
  }
}
