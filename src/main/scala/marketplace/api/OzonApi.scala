package marketplace.api

import tofu.syntax.handle._
import tofu.syntax.monadic._
import cats.syntax.traverse._
import cats.{~>, Functor}
import cats.free.Cofree
import cats.effect.Concurrent
import fs2.Stream
import tofu.lift.Lift
import tofu.fs2.LiftStream

import marketplace.marshalling._
import marketplace.clients.HttpClient
import marketplace.models.ozon.{Category, CategoryMenu, Request, SearchResultsV2, Url}

trait OzonApi[F[_], S[_]] {
  def getCategory(id: Category.Id): F[Option[Category]]
  def getCategoryMenu(id: Category.Id): F[Option[CategoryMenu]]
  def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[Option[SearchResultsV2]]
  def getCategories(rootId: Category.Id)(p: Category => Boolean): S[Category]
}

object OzonApi {

  final class Impl[F[_]: Concurrent: HttpClient: HttpClient.Handling] extends OzonApi[F, Stream[F, *]] {

    def getCategory(id: Category.Id): F[Option[Category]] = getCategoryMenu(id).map(_ >>= (_.category(id)))

    def getCategoryMenu(id: Category.Id): F[Option[CategoryMenu]] =
      HttpClient[F].send[CategoryMenu](Request.GetCategoryMenu(id)).restore

    def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[Option[SearchResultsV2]] =
      HttpClient[F].send[SearchResultsV2](Request.GetCategorySearchResultsV2(id, page = page)).restore

    def getCategories(rootId: Category.Id)(p: Category => Boolean): Stream[F, Category] = {
      def go(tree: Category.Tree[Stream[F, *]]): Stream[F, Category] =
        Option.when(p(tree.head))(tree.head) match {
          case None      => (tree.tailForced >>= go)
          case Some(cat) => Stream.emit(cat) ++ (tree.tailForced >>= go)
        }

      Stream.eval(getCategoryTree(rootId)) >>= (_.fold[Stream[F, Category]](Stream.empty)(go))
    }

    private def getCategoryTree(id: Category.Id): F[Option[Category.Tree[Stream[F, *]]]] =
      getCategory(id) >>= (_.traverse(
        Cofree
          .unfold[Stream[F, *], Category](_) { category =>
            val ids = category.children.keys.toList
            Stream.emits(ids).covary[F].parEvalMapUnordered(64)(getCategory).collect { case Some(subcategory) => subcategory }
          }
          .pure[F]
      ))
  }

  def make[
    F[_]: Concurrent: HttpClient: HttpClient.Handling,
    S[_]: LiftStream[*[_], F]
  ]: OzonApi[F, S] =
    bifunctorK.bimapK(new Impl[F])(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF)

  implicit val bifunctorK: BifunctorK[OzonApi] =
    new BifunctorK[OzonApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: OzonApi[F, G])(fw: F ~> W)(gq: G ~> Q): OzonApi[W, Q] =
        new OzonApi[W, Q] {
          def getCategory(id: Category.Id): W[Option[Category]]                                       = fw(ufg.getCategory(id))
          def getCategoryMenu(id: Category.Id): W[Option[CategoryMenu]]                               = fw(ufg.getCategoryMenu(id))
          def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): W[Option[SearchResultsV2]] = fw(ufg.getCategorySearchResultsV2(id, page))
          def getCategories(rootId: Category.Id)(p: Category => Boolean): Q[Category]                 = gq(ufg.getCategories(rootId)(p))
        }
    }
}
