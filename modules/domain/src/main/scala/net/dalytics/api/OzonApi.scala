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
import io.circe.Decoder
import supertagged.postfix._

import net.dalytics.marshalling._
import net.dalytics.clients.{HttpClient, HttpClientError}
import net.dalytics.models.ozon.{Category, CategoryMenu, Page, Request, Result, SearchFilter, SearchFilters, SearchResultsV2, SoldOutResultsV2}

trait OzonApi[F[_], S[_]] {
  def category(id: Category.Id): F[Option[Category]]
  def categories(rootId: Category.Id)(p: Category => Boolean): S[Category]
  def categoryMenu(id: Category.Id): F[Option[CategoryMenu]]
  def searchFilters(id: Category.Id, searchFilterKey: SearchFilter.Key): F[List[SearchFilter]]
  def searchPage(id: Category.Id, searchFilters: List[SearchFilter] = List.empty): F[Option[Page]]
  def searchResultsV2(id: Category.Id, page: Request.Page, searchFilters: List[SearchFilter] = List.empty): F[Option[SearchResultsV2]]
  def soldOutPage(id: Category.Id, searchFilters: List[SearchFilter] = List.empty): F[Option[Page]]
  def soldOutResultsV2(id: Category.Id, page: Request.SoldOutPage, searchFilters: List[SearchFilter] = List.empty): F[Option[SoldOutResultsV2]]
}

object OzonApi {

  private final class Impl[F[_]: Concurrent: Logging: HttpClient: HttpClient.Handling] extends OzonApi[F, Stream[F, *]] {
    def category(id: Category.Id): F[Option[Category]] =
      categoryMenu(id).map(_ >>= (_.category(id)))

    def categories(rootId: Category.Id)(p: Category => Boolean): Stream[F, Category] = {
      def go(tree: Category.Tree[Stream[F, *]]): Stream[F, Category] =
        Option.when(p(tree.head))(tree.head) match {
          case None      => (tree.tailForced >>= go)
          case Some(cat) => Stream.emit(cat) ++ (tree.tailForced >>= go)
        }

      Stream.eval(categoryTree(rootId)) >>= (_.fold[Stream[F, Category]](Stream.empty)(go))
    }

    private def categoryTree(id: Category.Id): F[Option[Category.Tree[Stream[F, *]]]] =
      category(id) >>= (_.traverse(
        Cofree
          .unfold[Stream[F, *], Category](_) { cat =>
            val ids = cat.children.keys.toList
            Stream.emits(ids).covary[F].parEvalMapUnordered(64)(category).collect { case Some(subcat) => subcat }
          }
          .pure[F]
      ))

    def categoryMenu(id: Category.Id): F[Option[CategoryMenu]] =
      get[Result](Request.GetCategoryMenu(id)).map(_ >>= (_.categoryMenu))

    def searchFilters(id: Category.Id, searchFilterKey: SearchFilter.Key): F[List[SearchFilter]] = {
      val request = Request.GetCategorySearchFilterValues(id, searchFilterKey)
      get[SearchFilters](request).map(_.fold(List.empty[SearchFilter])(_.values))
    }

    def searchPage(id: Category.Id, searchFilters: List[SearchFilter]): F[Option[Page]] = {
      val request = Request.GetCategorySearchResultsV2(id, 1 @@ Request.Page, searchFilters)
      get[Result](request).map(_ >>= (_.page))
    }

    def searchResultsV2(id: Category.Id, page: Request.Page, searchFilters: List[SearchFilter]): F[Option[SearchResultsV2]] = {
      val request = Request.GetCategorySearchResultsV2(id, page, searchFilters)
      get[Result](request).map(_ >>= (_.searchResultsV2))
    }

    def soldOutPage(id: Category.Id, searchFilters: List[SearchFilter]): F[Option[Page]] = {
      val request = Request.GetCategorySoldOutResultsV2(id, 1 @@ Request.SoldOutPage, searchFilters)
      get[Result](request).map(_ >>= (_.page))
    }

    def soldOutResultsV2(id: Category.Id, soldOutPage: Request.SoldOutPage, searchFilters: List[SearchFilter]): F[Option[SoldOutResultsV2]] = {
      val request = Request.GetCategorySoldOutResultsV2(id, soldOutPage, searchFilters)
      get[Result](request).map(_ >>= (_.soldOutResultsV2))
    }

    private def get[R: Decoder](request: Request): F[Option[R]] =
      HttpClient[F]
        .send[R](request)
        .recoverWith[HttpClientError] { case error: HttpClientError =>
          error"${error} was thrown while attempting to execute ${request}" *> error.raise[F, R]
        }
        .restore
  }

  def make[
    I[_]: Monad,
    F[_]: Concurrent: HttpClient: HttpClient.Handling,
    S[_]: LiftStream[*[_], F]
  ](implicit logs: Logs[I, F]): Resource[I, OzonApi[F, S]] =
    Resource.eval {
      logs
        .forService[OzonApi[F, S]]
        .map { implicit l =>
          val impl = new Impl[F]

          bifunctorK.bimapK(impl)(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF)
        }
    }

  implicit val bifunctorK: BifunctorK[OzonApi] =
    new BifunctorK[OzonApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: OzonApi[F, G])(fw: F ~> W)(gq: G ~> Q): OzonApi[W, Q] =
        new OzonApi[W, Q] {
          def category(id: Category.Id): W[Option[Category]]                                                                  = fw(ufg.category(id))
          def categories(id: Category.Id)(p: Category => Boolean): Q[Category]                                                = gq(ufg.categories(id)(p))
          def categoryMenu(id: Category.Id): W[Option[CategoryMenu]]                                                          = fw(ufg.categoryMenu(id))
          def searchFilters(id: Category.Id, key: SearchFilter.Key): W[List[SearchFilter]]                                    = fw(ufg.searchFilters(id, key))
          def searchPage(id: Category.Id, sfs: List[SearchFilter]): W[Option[Page]]                                           = fw(ufg.searchPage(id, sfs))
          def searchResultsV2(id: Category.Id, p: Request.Page, sfs: List[SearchFilter]): W[Option[SearchResultsV2]]          =
            fw(ufg.searchResultsV2(id, p, sfs))
          def soldOutPage(id: Category.Id, sfs: List[SearchFilter]): W[Option[Page]]                                          = fw(ufg.soldOutPage(id, sfs))
          def soldOutResultsV2(id: Category.Id, p: Request.SoldOutPage, sfs: List[SearchFilter]): W[Option[SoldOutResultsV2]] =
            fw(ufg.soldOutResultsV2(id, p, sfs))
        }
    }
}
