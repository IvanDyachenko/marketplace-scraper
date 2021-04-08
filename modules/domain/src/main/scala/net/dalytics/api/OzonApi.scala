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

import net.dalytics.marshalling._
import net.dalytics.clients.{HttpClient, HttpClientError}
import net.dalytics.models.ozon.{Category, CategoryMenu, Page, Request, Result, SearchFilter, SearchResultsV2, SoldOutResultsV2, Url}

trait OzonApi[F[_], S[_]] {
  def getCategory(id: Category.Id): F[Option[Category]]
  def getCategoryMenu(id: Category.Id): F[Option[CategoryMenu]]
  def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[Option[(Page, Category, SearchResultsV2)]]
  def getCategorySoldOutResultsV2(id: Category.Id, soldOutPage: Url.SoldOutPage): F[Option[(Page, Category, SoldOutResultsV2)]]
  def getCategorySearchFilterBrands(id: Category.Id): F[Option[SearchFilter.Brands]]
  def getCategories(rootId: Category.Id)(p: Category => Boolean): S[Category]
}

object OzonApi {

  private final class Impl[F[_]: Concurrent: Logging: HttpClient: HttpClient.Handling] extends OzonApi[F, Stream[F, *]] {

    def getCategory(id: Category.Id): F[Option[Category]] = getCategoryMenu(id).map(_ >>= (_.category(id)))

    def getCategoryMenu(id: Category.Id): F[Option[CategoryMenu]] = get[Result](Request.GetCategoryMenu(id)).map(_ >>= (_.categoryMenu))

    def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): F[Option[(Page, Category, SearchResultsV2)]] =
      get[Result](Request.GetCategorySearchResultsV2(id, page = page)).map(_ >>= (_.categorySearchResultsV2))

    def getCategorySoldOutResultsV2(id: Category.Id, soldOutPage: Url.SoldOutPage): F[Option[(Page, Category, SoldOutResultsV2)]] =
      get[Result](Request.GetCategorySoldOutResultsV2(id, soldOutPage = soldOutPage)).map(_ >>= (_.categorySoldOutResultsV2))

    def getCategorySearchFilterBrands(id: Category.Id): F[Option[SearchFilter.Brands]] =
      get[SearchFilter.Brands](Request.GetCategorySearchFilterBrands(id))

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
          def getCategory(id: Category.Id): W[Option[Category]]                                                                         = fw(ufg.getCategory(id))
          def getCategoryMenu(id: Category.Id): W[Option[CategoryMenu]]                                                                 = fw(ufg.getCategoryMenu(id))
          def getCategorySearchResultsV2(id: Category.Id, page: Url.Page): W[Option[(Page, Category, SearchResultsV2)]]                 =
            fw(ufg.getCategorySearchResultsV2(id, page))
          def getCategorySoldOutResultsV2(id: Category.Id, soldOutPage: Url.SoldOutPage): W[Option[(Page, Category, SoldOutResultsV2)]] =
            fw(ufg.getCategorySoldOutResultsV2(id, soldOutPage))
          def getCategorySearchFilterBrands(id: Category.Id): W[Option[SearchFilter.Brands]]                                            =
            fw(ufg.getCategorySearchFilterBrands(id))
          def getCategories(rootId: Category.Id)(p: Category => Boolean): Q[Category]                                                   = gq(ufg.getCategories(rootId)(p))
        }
    }
}
