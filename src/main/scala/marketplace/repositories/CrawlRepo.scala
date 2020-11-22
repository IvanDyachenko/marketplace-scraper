package marketplace.repositories

import cats.{Apply, Monad}
import cats.implicits._
import cats.effect.Resource
import tofu.higherKind.Mid
import derevo.derive
import tofu.data.derived.ContextEmbed
import tofu.higherKind.derived.representableK
import doobie.ConnectionIO
import doobie.util.log.LogHandler
import doobie.implicits._
import tofu.doobie.LiftConnectionIO
import tofu.doobie.log.EmbeddableLogHandler
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._

import marketplace.models.{Request, Response}

@derive(representableK)
trait CrawlRepo[DB[_]] {
  def add(req: Request, resp: Response): DB[Unit]
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
    def add(req: Request, resp: Response): ConnectionIO[Unit] =
      sql"""
           |INSERT INTO yandex.market_category_models
           |(uri, host, path, body_text)
           |VALUES
           |(
           |    ${req.uri.toString},
           |    ${req.host.toString},
           |    ${req.path.toString},
           |    ${resp.bodyText}
           |)
         """.stripMargin.update.run.void
  }

  final class LoggingMid[DB[_]: Apply: Logging] extends CrawlRepo[Mid[DB, *]] {
    def add(req: Request, resp: Response): Mid[DB, Unit] =
      trace"Send data to ClickHouse" *> _
  }
}
