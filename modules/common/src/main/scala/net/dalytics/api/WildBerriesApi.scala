package net.dalytics.api

import tofu.syntax.raise._
import tofu.syntax.handle._
import tofu.syntax.monadic._
import tofu.syntax.logging._
import cats.{~>, Functor, Monad}
import cats.effect.{Resource, Sync}
import tofu.logging.{Logging, Logs}
import fs2.Stream
import tofu.lift.Lift
import tofu.fs2.LiftStream
import io.circe.Decoder
import org.http4s.circe.jsonOf

import net.dalytics.marshalling._
import net.dalytics.clients.{HttpClient, HttpClientError}
import net.dalytics.models.wildberries.{Catalog, CatalogMenu, Request}

trait WildBerriesApi[F[_], S[_]] {
  def getCatalog(id: Catalog.Id): F[Option[Catalog]]
  def getCatalogs(rootId: Catalog.Id)(p: Catalog => Boolean): S[Catalog]
}

object WildBerriesApi {

  final class Impl[F[_]: Sync: Logging: HttpClient: HttpClient.Handling] extends WildBerriesApi[F, Stream[F, *]] {

    def getCatalog(id: Catalog.Id): F[Option[Catalog]] = getCatalogMenu.map(_ >>= (_.catalog(id)))

    def getCatalogs(rootId: Catalog.Id)(p: Catalog => Boolean): Stream[F, Catalog] =
      Stream.eval(getCatalog(rootId)) >>= (_.fold[Stream[F, Catalog]](Stream.empty)(catalog => Stream.emits(catalog.filter(p))))

    private def getCatalogMenu: F[Option[CatalogMenu]] = {
      val request = Request.GetCatalogMenu
      get[CatalogMenu](request)
    }

    private def get[R](request: Request)(implicit decoder: Decoder[R]): F[Option[R]] =
      HttpClient[F]
        .send[R](request)(jsonOf(Sync[F], decoder))
        .recoverWith[HttpClientError] { case error: HttpClientError =>
          error"${error} was thrown while attempting to execute ${request}" *> error.raise[F, R]
        }
        .restore
  }

  def make[
    I[_]: Monad,
    F[_]: Sync: HttpClient: HttpClient.Handling,
    S[_]: LiftStream[*[_], F]
  ](implicit logs: Logs[I, F]): Resource[I, WildBerriesApi[F, S]] =
    Resource.eval {
      logs
        .forService[WildBerriesApi[F, S]]
        .map(implicit l => bifunctorK.bimapK(new Impl[F])(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF))
    }

  implicit val bifunctorK: BifunctorK[WildBerriesApi] =
    new BifunctorK[WildBerriesApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: WildBerriesApi[F, G])(fw: F ~> W)(gq: G ~> Q): WildBerriesApi[W, Q] =
        new WildBerriesApi[W, Q] {
          def getCatalog(id: Catalog.Id): W[Option[Catalog]]                     = fw(ufg.getCatalog(id))
          def getCatalogs(rootId: Catalog.Id)(p: Catalog => Boolean): Q[Catalog] = gq(ufg.getCatalogs(rootId)(p))
        }
    }
}
