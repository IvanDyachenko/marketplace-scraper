package net.dalytics.db

import cats.{Functor, Monad}
import cats.syntax.functor._
import cats.effect.Resource
import tofu.lift.{Lift, UnliftIO}
import tofu.syntax.lift._
import tofu.logging.{Logging, Logs}
import doobie.util.log.{ExecFailure, LogEvent, ProcessingFailure, Success}
import tofu.doobie.log.{EmbeddableLogHandler, LogHandlerF}

object doobieLogging {

  def makeEmbeddableLogHandler[I[_]: Monad, F[_]: Functor: UnliftIO, DB[_]: Lift[F, *[_]]](
    name: String
  )(implicit logs: Logs[I, F]): Resource[I, EmbeddableLogHandler[DB]] =
    Resource.eval {
      logs.byName(name).map { implicit log =>
        val lhf = LogHandlerF(logDoobieEvent)
        EmbeddableLogHandler.async(lhf).lift[DB]
      }
    }

  private def logDoobieEvent[F[_]](implicit log: Logging[F]): LogEvent => F[Unit] = {
    case Success(s, a, e1, e2) =>
      log.info(
        s"""Successful Statement Execution:
           |
           |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
           |
           | arguments = [${a.mkString(", ")}]
           |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (${(e1 + e2).toMillis.toString} ms total)
          """.stripMargin
      )

    case ProcessingFailure(s, a, e1, e2, t) =>
      log.error(
        s"""Failed Resultset Processing:
           |
           |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
           |
           | arguments = [${a.mkString(", ")}]
           |   elapsed = ${e1.toMillis.toString} ms exec + ${e2.toMillis.toString} ms processing (failed) (${(e1 + e2).toMillis.toString} ms total)
           |   failure = ${t.getMessage}
          """.stripMargin
      )

    case ExecFailure(s, a, e1, t) =>
      log.error(
        s"""Failed Statement Execution:
           |
           |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
           |
           | arguments = [${a.mkString(", ")}]
           |   elapsed = ${e1.toMillis.toString} ms exec (failed)
           |   failure = ${t.getMessage}
          """.stripMargin
      )
  }
}
