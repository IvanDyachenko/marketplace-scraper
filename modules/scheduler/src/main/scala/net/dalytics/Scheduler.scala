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

import net.dalytics.config.{Config, TaskConfig, TasksConfig}
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
    commands: Stream[I, HandlerCommand],
    producer: KafkaProducer[I, Option[Command.Key], HandlerCommand]
  ) extends Scheduler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      commands
        .map(command => ProducerRecord(config.kafkaProducerConfig.topic("commands"), command.key, command))
        .evalMap(record => producer.produce(ProducerRecords.one(record)))
        .parEvalMap(config.kafkaProducerConfig.parallelism)(identity)
        .map(_.passthrough)
  }

  def make[
    I[_]: Monad: Concurrent,
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    commands: Stream[I, HandlerCommand],
    producer: KafkaProducer[I, Option[Command.Key], HandlerCommand]
  ): Resource[I, Scheduler[S]] =
    Resource.eval {
      Stream
        .eval {
          val impl: Scheduler[Stream[I, *]] = new Impl[I](config)(commands, producer)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }

  def makeCommands[F[_]: Concurrent: Timer](tasksConfig: TasksConfig)(
    wbApi: WildBerriesApi[F, Stream[F, *]],
    ozonApi: OzonApi[F, Stream[F, *]]
  ): Stream[F, HandlerCommand] =
    Stream
      .emits(tasksConfig.tasks)
      .map(taskConfig => Stream.awakeEvery[F](taskConfig.every).map(_ => taskConfig))
      .parJoinUnbounded
      .broadcastThrough(
        (taskConfigs: Stream[F, TaskConfig]) =>
          taskConfigs
            .collect { case taskConfig: TaskConfig.OzonSeller => taskConfig }
            .flatMap { case TaskConfig.OzonSeller(pageLimit, _) =>
              Stream.range(1, pageLimit + 1).parEvalMapUnordered[F, HandlerCommand](pageLimit) { n =>
                HandlerCommand.handleOzonRequest[F](ozon.Request.GetSellerList(n @@ ozon.Request.Page))
              }
            },
        (taskConfigs: Stream[F, TaskConfig]) =>
          taskConfigs
            .collect { case taskConfig: TaskConfig.OzonCategory => taskConfig }
            .map { case TaskConfig.OzonCategory(rootCategoryId, splitBy, every) =>
              ozonApi.categories(rootCategoryId)(_.isLeaf).map(category => TaskConfig.OzonCategory(category.id, splitBy, every))
            }
            .parJoinUnbounded
            .map { case TaskConfig.OzonCategory(categoryId, splitBy, _) =>
              ozonApi
                .searchFilters(categoryId, splitBy)
                .parEvalMapUnordered(256)(searchFilter => ozonApi.searchPage(categoryId, List(searchFilter)).map(searchFilter -> _))
                .flatMap {
                  case (searchFilter, Some(ozon.Page(_, totalPages, _))) if totalPages > 0 =>
                    val filters = List(searchFilter)
                    Stream.range(1, totalPages.min(ozon.Page.MaxValue) + 1).covary[F].parEvalMapUnordered(ozon.Page.MaxValue) { n =>
                      val request = ozon.Request.GetCategorySearchResultsV2(categoryId, n @@ ozon.Request.Page, filters)
                      HandlerCommand.handleOzonRequest[F](request)
                    }
                  case _                                                                   => Stream.empty
                }
            }
            .parJoin(256)
      )
}
