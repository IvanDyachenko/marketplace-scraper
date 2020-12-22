package marketplace.services

import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.Raise
import tofu.syntax.raise._
import tofu.syntax.monadic._
import supertagged.postfix._
import derevo.derive
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.{Logging, Logs}
import tofu.syntax.logging._
import tofu.generate.GenUUID
import io.circe.DecodingFailure

import marketplace.models.{EventId, EventKey, Timestamp}
import marketplace.models.parser.{Command, Event, ParseYandexMarketResponse, YandexMarketResponseParsed}
import marketplace.models.yandex.market.{Result => YandexMarketResult}

@derive(representableK)
trait Parse[F[_]] {
  def handle(command: Command): F[Event]
}

object Parse {

  private final class Logger[F[_]: Monad: Logging] extends Parse[Mid[F, *]] {
    def handle(command: Command): Mid[F, Event] =
      info"Execution of the ${command} has started" *> _
  }

  private final class Impl[F[_]: Monad: Clock: GenUUID: Raise[*[_], DecodingFailure]] extends Parse[F] {
    def handle(command: Command): F[Event] = command match {
      case ParseYandexMarketResponse(_, _, _, response) =>
        for {
          uuid    <- GenUUID[F].randomUUID
          instant <- Clock[F].instantNow
          result  <- response.as[YandexMarketResult].toRaise
        } yield YandexMarketResponseParsed(uuid @@ EventId, "yandex.market" @@ EventKey, Timestamp(instant), result)
    }
  }

  def apply[F[_]](implicit ev: Parse[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Clock: GenUUID: Raise[*[_], DecodingFailure]](implicit logs: Logs[I, F]): Resource[I, Parse[F]] =
    Resource.liftF {
      logs
        .forService[Parse[F]]
        .map { implicit l =>
          val impl = new Impl[F]

          val logger: Parse[Mid[F, *]] = new Logger[F]

          logger.attach(impl)
        }
    }
}
