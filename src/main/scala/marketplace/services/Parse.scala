package marketplace.services

import scala.util.control.NoStackTrace

import tofu.syntax.raise._
import tofu.syntax.monadic._
import tofu.syntax.logging._
import derevo.derive
import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.{Handle, Raise}
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.generate.GenUUID
import tofu.logging.{Logging, Logs}

import marketplace.models.parser.{ParserEvent => Event, ParserCommand => Command, ParseOzonResponse, ParseYandexMarketResponse}
import marketplace.models.ozon.{Result => OzonResult}
import marketplace.models.yandex.market.{Result => YandexMarketResult}

@derive(representableK)
trait Parse[F[_]] {
  def handle(command: Command): F[Event]
}

object Parse {
  sealed trait ParsingError extends NoStackTrace
  object ParsingError {
    type Handling[F[_]] = Handle[F, ParsingError]
    type Raising[F[_]]  = Raise[F, ParsingError]

    case class DecodingError(message: String) extends ParsingError
  }

  private final class Logger[F[_]: Monad: Logging] extends Parse[Mid[F, *]] {
    def handle(command: Command): Mid[F, Event] =
      info"Execution of the ${command} has started" *> _ <* info"Execution of the ${command} has been completed"
  }

  private final class Impl[F[_]: Monad: Clock: GenUUID: ParsingError.Raising] extends Parse[F] {
    def handle(command: Command): F[Event] = command match {
      case ParseOzonResponse(_, _, _, response)         =>
        response.as[OzonResult].left.map(failure => ParsingError.DecodingError(failure.toString)).toRaise >>= (result =>
          Event.ozonResponseParsed(result)
        )
      case ParseYandexMarketResponse(_, _, _, response) =>
        response.as[YandexMarketResult].left.map(failure => ParsingError.DecodingError(failure.toString)).toRaise >>= (result =>
          Event.yandexMarketResponseParsed(result)
        )
    }
  }

  def apply[F[_]](implicit ev: Parse[F]): ev.type = ev

  def make[I[_]: Monad, F[_]: Monad: Clock: GenUUID: ParsingError.Raising](implicit logs: Logs[I, F]): Resource[I, Parse[F]] =
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
