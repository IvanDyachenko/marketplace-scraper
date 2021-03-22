package net.dalytics

import cats.tagless.syntax.functorK._
import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import tofu.syntax.embed._
import tofu.syntax.monadic._
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords}
import supertagged.postfix._

import net.dalytics.config.{Config, SourceConfig}
import net.dalytics.models.{ozon, Command}
import net.dalytics.models.handler.HandlerCommand
import net.dalytics.api.{OzonApi, WildBerriesApi}

@derive(representableK)
trait Scheduler[S[_]] {
  def run: S[Unit]
}

object Scheduler {
  def apply[F[_]](implicit ev: Scheduler[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Concurrent
  ](config: Config)(
    sourcesOfCommands: List[Stream[I, HandlerCommand]],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], HandlerCommand]
  ) extends Scheduler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      Stream
        .emits(sourcesOfCommands)
        .parJoinUnbounded
        .map(command => ProducerRecord(config.kafkaProducerConfig.topic("commands"), command.key, command))
        .evalMap(record => producerOfCommands.produce(ProducerRecords.one(record)))
        .parEvalMap(config.kafkaProducerConfig.maxBufferSize)(identity)
        .map(_.passthrough)
  }

  def make[
    I[_]: Monad: Concurrent,
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    sourcesOfCommands: List[Stream[I, HandlerCommand]],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], HandlerCommand]
  ): Resource[I, Scheduler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Scheduler[Stream[I, *]] = new Impl[I](config)(sourcesOfCommands, producerOfCommands)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }

  def makeCommandsSource[F[_]: Timer: Concurrent](sourceConfig: SourceConfig)(
    wbApi: WildBerriesApi[F, Stream[F, *]],
    ozonApi: OzonApi[F, Stream[F, *]]
  ): Stream[F, HandlerCommand] =
    sourceConfig match {
      case SourceConfig.WbCatalog(_, _)                     => ???
      case SourceConfig.OzonSeller(every)                   =>
        Stream
          .awakeEvery[F](every)
          .flatMap { _ =>
            Stream.range(1, 450).parEvalMapUnordered[F, HandlerCommand](1000) { p =>
              HandlerCommand.handleOzonRequest[F](ozon.Request.GetSellerList(p @@ ozon.Url.Page))
            }
          }
      case SourceConfig.OzonCategory(rootCategoryId, every) =>
        Stream
          .awakeEvery[F](every)
          .flatMap { _ =>
            ozonApi
              .getCategories(rootCategoryId)(_ => true)
              .parEvalMapUnordered(256) { category =>
                if (category.isLeaf)
                  ozonApi
                    .getCategorySearchResultsV2(category.id, 1 @@ ozon.Url.Page)
                    .map(category -> _.fold[Int](10) {
                      _ match {
                        case ozon.CategorySearchResultsV2.Failure(error)      => 10
                        case ozon.CategorySearchResultsV2.Success(_, page, _) => page.total.min(278)
                      }
                    })
                else
                  (category, 1).pure[F]
              }
              .flatMap { case (category: ozon.Category, page: Int) =>
                Stream
                  .range(1, page + 1)
                  .parEvalMapUnordered[F, HandlerCommand](1000) { p =>
                    HandlerCommand.handleOzonRequest[F](ozon.Request.GetCategorySearchResultsV2(category.id, category.name, p @@ ozon.Url.Page))
                  }
              }
          }
    }
}
