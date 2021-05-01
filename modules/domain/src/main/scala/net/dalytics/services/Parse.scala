package net.dalytics.services

import scala.util.control.NoStackTrace

import cats.syntax.traverse._
import cats.Monad
import cats.effect.{Clock, Resource}
import tofu.syntax.monadic._
import tofu.syntax.logging._
import derevo.derive
import tofu.higherKind.Mid
import tofu.higherKind.derived.representableK
import tofu.logging.derivation.loggable
import tofu.logging.{Logging, Logs}
import tofu.{Handle, Raise}
import tethys.JsonReader
import tethys._
import tethys.jackson._

import net.dalytics.models.{ozon, Raw}
import net.dalytics.models.parser.{ParserEvent => Event, ParserCommand => Command}

@derive(representableK)
trait Parse[F[_]] {
  def handle(command: Command): F[Parse.Result]
}

object Parse {
  def apply[F[_]](implicit ev: Parse[F]): ev.type = ev

  @derive(loggable)
  sealed trait ParsingError extends NoStackTrace

  object ParsingError {
    type Raising[F[_]]  = Raise[F, ParsingError]
    type Handling[F[_]] = Handle[F, ParsingError]

    @derive(loggable)
    case class DecodingError(message: String)    extends ParsingError
    @derive(loggable)
    case class UnexpectedResult(message: String) extends ParsingError

    def decodingError(message: String): ParsingError    = DecodingError(message)
    def unexpectedResult(message: String): ParsingError = UnexpectedResult(message)
  }

  final type Result = Either[ParsingError, List[Event]]

  private final class Logger[F[_]: Monad: Logging] extends Parse[Mid[F, *]] {
    def handle(command: Command): Mid[F, Result] =
      trace"Execution of the ${command} has started" *> _ flatTap {
        case Left(error)  => error"Execution of the ${command} has been completed with the ${error}"
        case Right(event) => trace"${event} has been successfully created as a result of execution of the ${command}"
      }
  }

  private final class Impl[F[_]: Monad: Clock] extends Parse[F] {
    def handle(command: Command): F[Result] = command match {
      case Command.ParseOzonResponse(created, response) => // format: off
        parse[ozon.Result](response) >>= (_.traverse { result => // format: on
          for {
            sellerListParsed               <- Event.OzonSellerListItemParsed(created, result)
            categorySearchResultsV2Parsed  <- Event.OzonCategorySearchResultsV2ItemParsed(created, result)
            categorySoldOutResultsV2Parsed <- Event.OzonCategorySoldOutResultsV2ItemParsed(created, result)
          } yield List(sellerListParsed, categorySearchResultsV2Parsed, categorySoldOutResultsV2Parsed).flatten
        })
    }

    private def parse[R: JsonReader](raw: Raw): F[Either[ParsingError, R]] =
      raw.jsonAs[R].left.map(failure => ParsingError.decodingError(failure.getMessage)).pure[F]
  }

  def make[
    I[_]: Monad,
    F[_]: Monad: Clock
  ](implicit logs: Logs[I, F]): Resource[I, Parse[F]] =
    Resource.eval {
      logs
        .forService[Parse[F]]
        .map { implicit l =>
          val impl = new Impl[F]

          val logger: Parse[Mid[F, *]] = new Logger[F]

          logger.attach(impl)
        }
    }
}
