package marketplace.services

import scala.util.control.NoStackTrace

import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.syntax.raise._
import tofu.syntax.monadic._
import tofu.syntax.logging._
import tofu.{Handle, Raise}
import tofu.higherKind.Mid
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.generate.GenUUID
import tofu.logging.{Logging, Logs}
import io.circe.{Decoder, Json}

import marketplace.models.ozon.{Result => OzonResult}
import marketplace.models.parser.{ParserEvent => Event, ParserCommand => Command}

@derive(representableK)
trait Parse[F[_]] {
  def handle(command: Command): F[Event]
}

object Parse {
  sealed trait ParsingError extends NoStackTrace

  object ParsingError {
    type Raising[F[_]]  = Raise[F, ParsingError]
    type Handling[F[_]] = Handle[F, ParsingError]

    case class DecodingError(message: String) extends ParsingError

    def decodingError(message: String): ParsingError = DecodingError(message)
  }

  private final type Result[A] = Either[ParsingError, A]

  private final class Logger[F[_]: Monad: Logging] extends Parse[Mid[F, *]] {
    def handle(command: Command): Mid[F, Event] =
      debug"Execution of the ${command} has started" *> _ <* debug"Execution of the ${command} has been completed"
  }

  private final class Impl[F[_]: Monad: Clock: GenUUID: ParsingError.Raising] extends Parse[F] {
    def handle(command: Command): F[Event] = command match {
      case Command.ParseOzonResponse(_, _, created, response) =>
        parse[OzonResult](response) >>= (_.toRaise) >>= (Event.ozonResponseParsed(created, _))
    }

    private def parse[R: Decoder](data: Json): F[Result[R]] =
      data.as[R].left.map(failure => ParsingError.decodingError(failure.toString)).pure[F]
  }

  def apply[F[_]](implicit ev: Parse[F]): ev.type = ev

  def make[
    I[_]: Monad,
    F[_]: Monad: Clock: GenUUID: ParsingError.Raising
  ](implicit logs: Logs[I, F]): Resource[I, Parse[F]] =
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
