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
import net.dalytics.models.ozon.{Category, CategoryMenu, Request, Result, SearchFilter, SearchFilters, SearchPage, SearchResultsV2, SoldOutPage, SoldOutResultsV2, Url}

trait OzonApi[F[_], S[_]] {
  def category(id: Category.Id): F[Option[Category]]
  def categories(rootId: Category.Id)(p: Category => Boolean): S[Category]
  def categoryMenu(id: Category.Id): F[Option[CategoryMenu]]
  def searchFilters(id: Category.Id, searchFilterKey: SearchFilter.Key): S[SearchFilter]
  def searchPage(id: Category.Id): F[Option[SearchPage]]
  def searchPage(id: Category.Id, searchFilter: SearchFilter): F[Option[SearchPage]]
  def searchResultsV2(id: Category.Id, page: Url.Page, searchFilter: Option[SearchFilter] = None): F[Option[SearchResultsV2]]
  def soldOutPage(id: Category.Id): F[Option[SoldOutPage]]
  def soldOutPage(id: Category.Id, searchFilter: SearchFilter): F[Option[SoldOutPage]]
  def soldOutResultsV2(id: Category.Id, page: Url.SoldOutPage, searchFilter: Option[SearchFilter] = None): F[Option[SoldOutResultsV2]]
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
            Stream.emits(ids).covary[F].parEvalMapUnordered(128)(category).collect { case Some(subcat) => subcat }
          }
          .pure[F]
      ))

    def categoryMenu(id: Category.Id): F[Option[CategoryMenu]] =
      get[Result](Request.GetCategoryMenu(id)).map(_ >>= (_.categoryMenu))

    def searchPage(id: Category.Id): F[Option[SearchPage]] = searchPage(id, None)

    def searchPage(id: Category.Id, searchFilter: SearchFilter): F[Option[SearchPage]] = searchPage(id, Some(searchFilter))

    private def searchPage(id: Category.Id, searchFilter: Option[SearchFilter]): F[Option[SearchPage]] = {
      val request = Request.GetCategorySearchResultsV2(id, 1 @@ Url.Page, searchFilter)
      get[Result](request).map(_ >>= (_.searchPage))
    }

    def searchResultsV2(id: Category.Id, page: Url.Page, searchFilter: Option[SearchFilter]): F[Option[SearchResultsV2]] = {
      val request = Request.GetCategorySearchResultsV2(id, page, searchFilter)
      get[Result](request).map(_ >>= (_.searchResultsV2))
    }

    def soldOutPage(id: Category.Id): F[Option[SoldOutPage]] = soldOutPage(id, None)

    def soldOutPage(id: Category.Id, searchFilter: SearchFilter): F[Option[SoldOutPage]] = soldOutPage(id, Some(searchFilter))

    private def soldOutPage(id: Category.Id, searchFilter: Option[SearchFilter]): F[Option[SoldOutPage]] = {
      val request = Request.GetCategorySoldOutResultsV2(id, soldOutPage = 1 @@ Url.SoldOutPage, searchFilter = searchFilter)
      get[Result](request).map(_ >>= (_.soldOutPage))
    }

    def searchFilters(id: Category.Id, searchFilterKey: SearchFilter.Key): Stream[F, SearchFilter] = {
      val request = Request.GetCategorySearchFilterValues(id, searchFilterKey = searchFilterKey)

      Stream.eval(get[SearchFilters](request)) >>= { filtersOpt =>
        val filters = filtersOpt.fold(List.empty[SearchFilter])(_.values)
        Stream.emits(filters)
      }
    }

    def soldOutResultsV2(id: Category.Id, page: Url.SoldOutPage, searchFilter: Option[SearchFilter]): F[Option[SoldOutResultsV2]] = {
      val request = Request.GetCategorySoldOutResultsV2(id, soldOutPage = page, searchFilter = searchFilter)
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
        .map(implicit l => bifunctorK.bimapK(new Impl[F])(Lift.liftIdentity[F].liftF)(LiftStream[S, F].liftF))
    }

  implicit val bifunctorK: BifunctorK[OzonApi] =
    new BifunctorK[OzonApi] {
      def bimapK[F[_]: Functor, G[_]: Functor, W[_], Q[_]](ufg: OzonApi[F, G])(fw: F ~> W)(gq: G ~> Q): OzonApi[W, Q] =
        new OzonApi[W, Q] {
          def category(id: Category.Id): W[Option[Category]]                                                               = fw(ufg.category(id))
          def categories(id: Category.Id)(p: Category => Boolean): Q[Category]                                             = gq(ufg.categories(id)(p))
          def categoryMenu(id: Category.Id): W[Option[CategoryMenu]]                                                       = fw(ufg.categoryMenu(id))
          def searchFilters(id: Category.Id, key: SearchFilter.Key): Q[SearchFilter]                                       = gq(ufg.searchFilters(id, key))
          def searchPage(id: Category.Id): W[Option[SearchPage]]                                                           = fw(ufg.searchPage(id))
          def searchPage(id: Category.Id, sf: SearchFilter): W[Option[SearchPage]]                                         = fw(ufg.searchPage(id, sf))
          def searchResultsV2(id: Category.Id, p: Url.Page, sf: Option[SearchFilter]): W[Option[SearchResultsV2]]          = fw(ufg.searchResultsV2(id, p, sf))
          def soldOutPage(id: Category.Id): W[Option[SoldOutPage]]                                                         = fw(ufg.soldOutPage(id))
          def soldOutPage(id: Category.Id, sf: SearchFilter): W[Option[SoldOutPage]]                                       = fw(ufg.soldOutPage(id, sf))
          def soldOutResultsV2(id: Category.Id, p: Url.SoldOutPage, sf: Option[SearchFilter]): W[Option[SoldOutResultsV2]] =
            fw(ufg.soldOutResultsV2(id, p, sf))
        }
    }
}
