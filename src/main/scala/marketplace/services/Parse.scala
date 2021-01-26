package marketplace.services

import scala.util.control.NoStackTrace

import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.syntax.monadic._
import tofu.syntax.logging._
import tofu.{Handle, Raise}
import tofu.higherKind.Mid
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.generate.GenUUID
import tofu.logging.{Logging, Logs}
import tofu.logging.derivation.loggable
import io.circe.{Decoder, Json}

import marketplace.models.ozon.{Result => OzonResult}
import marketplace.models.parser.{ParserEvent => Event, ParserCommand => Command}

@derive(representableK)
trait Parse[F[_]] {
  def handle(command: Command): F[Parse.Result]
}

object Parse {
  @derive(loggable)
  sealed trait ParsingError extends NoStackTrace

  object ParsingError {
    type Raising[F[_]]  = Raise[F, ParsingError]
    type Handling[F[_]] = Handle[F, ParsingError]

    @derive(loggable)
    case class DecodingError(message: String) extends ParsingError

    def decodingError(message: String): ParsingError = DecodingError(message)
  }

  private final type Result = Either[ParsingError, Event]

  private final class Logger[F[_]: Monad: Logging] extends Parse[Mid[F, *]] {
    def handle(command: Command): Mid[F, Result] =
      debug"Execution of the ${command} has started" *> _.flatTap {
        case Left(error)  => error"Execution of the ${command} has been completed with an error: ${error}"
        case Right(event) => debug"${event} has been successfully created as a result of execution of the ${command}"
      }
  }

  private final class Impl[F[_]: Monad: Clock: GenUUID] extends Parse[F] {
    def handle(command: Command): F[Result] = command match {
      case Command.ParseOzonResponse(_, _, created, response) =>
        parse[OzonResult](response) >>= (_ match {
          case Left(error)   => (Left(error): Result).pure[F]
          case Right(result) => Event.ozonResponseParsed(created, result).map(Right(_))
        })
    }

    private def parse[R: Decoder](data: Json): F[Either[ParsingError, R]] =
      data.as[R].left.map(failure => ParsingError.decodingError(failure.toString)).pure[F]
  }

  def apply[F[_]](implicit ev: Parse[F]): ev.type = ev

  def make[
    I[_]: Monad,
    F[_]: Monad: Clock: GenUUID
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
