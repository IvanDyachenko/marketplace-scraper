package marketplace.api

import cats.{~>, Functor}
import cats.free.Cofree
import cats.effect.Concurrent
import cats.syntax.traverse._
import tofu.syntax.monadic._
import fs2.Stream
import tofu.lift.Lift
import tofu.fs2.LiftStream

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.models.ozon.{Category, CategoryMenu, Request, SearchResultsV2, Url}

trait OzonApi[F[_], S[_]] {
  def getCategory(id: Category.Id): F[Option[Category]]
  def getCategoryMenu(id: Category.Id): F[CategoryMenu]
  def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[SearchResultsV2]
  def getCategories(rootId: Category.Id)(p: Category => Boolean): S[Category]
}

object OzonApi {

  final class Impl[F[_]: Functor: Concurrent: HttpClient: HttpClient.Raising] extends OzonApi[F, Stream[F, *]] {

    def getCategory(id: Category.Id): F[Option[Category]] = getCategoryMenu(id).map(_.category(id))

    def getCategoryMenu(id: Category.Id): F[CategoryMenu] =
      HttpClient[F].send[CategoryMenu](Request.GetCategoryMenu(id))

    def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[SearchResultsV2] =
      HttpClient[F].send[SearchResultsV2](Request.GetCategorySearchResultsV2(id, page))

    def getCategories(rootId: Category.Id)(p: Category => Boolean): Stream[F, Category] = {
      def go(tree: Category.Tree[Stream[F, *]]): Stream[F, Category] =
        Option.when(p(tree.head))(tree.head) match {
          case None      => (tree.tailForced >>= go)
          case Some(cat) => Stream.emit(cat) ++ (tree.tailForced >>= go)
        }

      Stream.eval(getCategoryTree(rootId).map(_.get)) >>= (tree => go(tree))
    }

    private def getCategoryTree(id: Category.Id): F[Option[Category.Tree[Stream[F, *]]]] =
      getCategory(id) >>= (_.traverse(
        Cofree
          .unfold[Stream[F, *], Category](_) { category =>
            val ids = category.children.keys.toList
            Stream.emits(ids).covary[F].parEvalMapUnordered(1000)(getCategory).collect { case Some(subcategory) => subcategory }
          }
          .pure[F]
      ))
  }

  def make[
    F[_]: Functor: Concurrent: HttpClient: HttpClient.Raising,
    S[_]: Functor: LiftStream[*[_], F]
  ]: OzonApi[F, S] =
    bifunctorK.bimapK(new Impl[F])(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF)

  trait BifunctorK[U[f[_], g[_]]] {
    def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: U[F, G])(fw: F ~> W)(gq: G ~> Q): U[W, Q]
  }

  implicit val bifunctorK: BifunctorK[OzonApi] =
    new BifunctorK[OzonApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: OzonApi[F, G])(fw: F ~> W)(gq: G ~> Q): OzonApi[W, Q] =
        new OzonApi[W, Q] {
          def getCategory(id: Category.Id): W[Option[Category]]                               = fw(ufg.getCategory(id))
          def getCategoryMenu(id: Category.Id): W[CategoryMenu]                               = fw(ufg.getCategoryMenu(id))
          def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): W[SearchResultsV2] = fw(ufg.getCategorySearchResultsV2(id, page))
          def getCategories(rootId: Category.Id)(p: Category => Boolean): Q[Category]         = gq(ufg.getCategories(rootId)(p))
        }
    }
}
