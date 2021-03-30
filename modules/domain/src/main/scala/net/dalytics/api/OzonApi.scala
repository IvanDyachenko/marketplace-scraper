package net.dalytics.api

import tofu.syntax.raise._
import tofu.syntax.handle._
import tofu.syntax.monadic._
import tofu.syntax.logging._
import cats.syntax.traverse._
import cats.{~>, Functor, Monad}
import cats.free.Cofree
import cats.effect.{Concurrent, Resource}
import tofu.logging.{Logging, Logs}
import fs2.Stream
import tofu.lift.Lift
import tofu.fs2.LiftStream

import net.dalytics.marshalling._
import net.dalytics.clients.{HttpClient, HttpClientError}
import net.dalytics.models.ozon.{Category, CategoryMenu, Page, Request, Result, SearchResultsV2, Url}
import net.dalytics.models.ozon.Catalog

trait OzonApi[F[_], S[_]] {
  def getCategory(id: Category.Id): F[Option[Category]]
  def getCategoryMenu(id: Category.Id): F[Option[CategoryMenu]]
  def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[Option[(Page, Category, SearchResultsV2)]]
  def getCategories(rootId: Category.Id)(p: Category => Boolean): S[Category]
}

object OzonApi {

  private final class Impl[F[_]: Concurrent: Logging: HttpClient: HttpClient.Handling] extends OzonApi[F, Stream[F, *]] {

    def getCategory(id: Category.Id): F[Option[Category]] = getCategoryMenu(id).map(_ >>= (_.category(id)))

    def getCategoryMenu(id: Category.Id): F[Option[CategoryMenu]] = {
      val request = Request.GetCategoryMenu(id)

      HttpClient[F]
        .send[Result](request)
        .recoverWith[HttpClientError] { case error: HttpClientError =>
          error"Error was thrown while attempting to execute ${request}. ${error}" *> error.raise[F, Result]
        }
        .restore
        .map(_ >>= (_.categoryMenu))
    }

    def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[Option[(Page, Category, SearchResultsV2)]] = {
      val request = Request.GetCategorySearchResultsV2(id, page = page)

      HttpClient[F]
        .send[Result](request)
        .recoverWith[HttpClientError] { case error: HttpClientError =>
          error"Error was thrown while attempting to execute ${request}. ${error}" *> error.raise[F, Result]
        }
        .restore
        .map(_ >>= {
          case Result(_, Some(Catalog(page, category, _, Some(searchResultsV2)))) => Some((page, category, searchResultsV2))
          case _                                                                  => None
        })
    }

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
            Stream.emits(ids).covary[F].parEvalMapUnordered(128)(getCategory).collect { case Some(subcategory) => subcategory }
          }
          .pure[F]
      ))
  }

  def make[
    I[_]: Monad,
    F[_]: Concurrent: HttpClient: HttpClient.Handling,
    S[_]: LiftStream[*[_], F]
  ](implicit logs: Logs[I, F]): Resource[I, OzonApi[F, S]] =
    Resource.liftF {
      logs
        .forService[OzonApi[F, S]]
        .map(implicit l => bifunctorK.bimapK(new Impl[F])(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF))
    }

  implicit val bifunctorK: BifunctorK[OzonApi] =
    new BifunctorK[OzonApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: OzonApi[F, G])(fw: F ~> W)(gq: G ~> Q): OzonApi[W, Q] =
        new OzonApi[W, Q] {
          def getCategory(id: Category.Id): W[Option[Category]]                                                         = fw(ufg.getCategory(id))
          def getCategoryMenu(id: Category.Id): W[Option[CategoryMenu]]                                                 = fw(ufg.getCategoryMenu(id))
          def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): W[Option[(Page, Category, SearchResultsV2)]] =
            fw(ufg.getCategorySearchResultsV2(id, page))
          def getCategories(rootId: Category.Id)(p: Category => Boolean): Q[Category]                                   = gq(ufg.getCategories(rootId)(p))
        }
    }
}
