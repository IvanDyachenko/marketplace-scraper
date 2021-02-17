package marketplace.api

import cats.{~>, Functor}
import tofu.syntax.monadic._
import fs2.Stream
import tofu.lift.Lift
import tofu.fs2.LiftStream

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.models.wildberries.{Catalog, CatalogMenu, Request}

trait WildBerriesApi[F[_], S[_]] {
  def getCatalog(id: Catalog.Id): F[Option[Catalog]]
  def getCatalogs(rootId: Catalog.Id)(p: Catalog => Boolean): S[Catalog]
}

object WildBerriesApi {
  final class Impl[F[_]: Functor: HttpClient: HttpClient.Raising] extends WildBerriesApi[F, Stream[F, *]] {
    def getCatalog(id: Catalog.Id): F[Option[Catalog]] =
      getCatalogMenu.map(_.catalog(id))

    def getCatalogs(rootId: Catalog.Id)(p: Catalog => Boolean): Stream[F, Catalog] =
      Stream.eval(getCatalog(rootId)).map(_.get) >>= (cat => Stream.emits(cat.filter(p)))

    private def getCatalogMenu: F[CatalogMenu] =
      HttpClient[F].send[CatalogMenu](Request.GetWildBerriesMenu)
  }

  def make[
    F[_]: Functor: HttpClient: HttpClient.Raising,
    S[_]: Functor: LiftStream[*[_], F]
  ]: WildBerriesApi[F, S] =
    bifunctorK.bimapK(new Impl[F])(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF)

  implicit val bifunctorK: BifunctorK[WildBerriesApi] =
    new BifunctorK[WildBerriesApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: WildBerriesApi[F, G])(fw: F ~> W)(gq: G ~> Q): WildBerriesApi[W, Q] =
        new WildBerriesApi[W, Q] {
          def getCatalog(id: Catalog.Id): W[Option[Catalog]]                     = fw(ufg.getCatalog(id))
          def getCatalogs(rootId: Catalog.Id)(p: Catalog => Boolean): Q[Catalog] = gq(ufg.getCatalogs(rootId)(p))
        }
    }
}
