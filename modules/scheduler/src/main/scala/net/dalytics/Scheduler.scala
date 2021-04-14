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
        .parEvalMap(config.kafkaProducerConfig.parallelism)(identity)
        .map(_.passthrough)
  }

  def make[
    I[_]: Monad: Concurrent,
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    sourcesOfCommands: List[Stream[I, HandlerCommand]],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], HandlerCommand]
  ): Resource[I, Scheduler[S]] =
    Resource.eval {
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
      case SourceConfig.WbCatalog(_, _)                                      => ???
      case SourceConfig.OzonSeller(pageLimit, every)                         =>
        Stream
          .awakeEvery[F](every)
          .flatMap { _ =>
            Stream.range(1, pageLimit + 1).parEvalMapUnordered[F, HandlerCommand](pageLimit) { page =>
              HandlerCommand.handleOzonRequest[F](ozon.Request.GetSellerList(page @@ ozon.Request.Page))
            }
          }
      case SourceConfig.OzonCategory(rootCategoryId, searchFilterKey, every) =>
        Stream
          .awakeEvery[F](every)
          .flatMap { _ =>
            ozonApi
              .categories(rootCategoryId)(_ => true)
              .broadcastThrough(
                (stream: Stream[F, ozon.Category]) =>
                  stream
                    .parEvalMapUnordered(256)(category => ozonApi.searchPage(category.id).map(category -> _))
                    .collect {
                      case (category, Some(page)) if category.isLeaf => (category, page.total.min(278))
                      case (category, _)                             => (category, 1)
                    }
                    .flatMap { case (category, totalPages) =>
                      Stream.range(1, totalPages + 1).covary[F].parEvalMapUnordered(278) { n =>
                        val request = ozon.Request.GetCategorySearchResultsV2(category.id, n @@ ozon.Request.Page, List.empty)
                        HandlerCommand.handleOzonRequest[F](request)
                      }
                    },
                (stream: Stream[F, ozon.Category]) =>
                  stream
                    .parEvalMapUnordered(256)(category => ozonApi.soldOutPage(category.id).map(category -> _))
                    .collect {
                      case (category, Some(page)) if category.isLeaf => (category, page.total.min(278))
                      case (category, _)                             => (category, 1)
                    }
                    .flatMap { case (category, totalPages) =>
                      Stream.range(1, totalPages + 1).covary[F].parEvalMapUnordered(278) { n =>
                        val request = ozon.Request.GetCategorySoldOutResultsV2(category.id, n @@ ozon.Request.SoldOutPage, List.empty)
                        HandlerCommand.handleOzonRequest[F](request)
                      }
                    }
              )
          }
    }
}
