package net.dalytics.modules

import cats.tagless.syntax.functorK._
import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.WithRun
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{commitBatchWithin, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}
import supertagged.postfix._

import net.dalytics.config.{HandlerConfig, SchedulerConfig, SourceConfig}
import net.dalytics.context.AppContext
import net.dalytics.api.{OzonApi, WildBerriesApi}
import net.dalytics.services.Handle
import net.dalytics.models.{ozon, Command, Event}
import net.dalytics.models.handler.{HandlerCommand, HandlerEvent}

@derive(representableK)
trait Handler[S[_]] {
  def run: S[Unit]
  def schedule: S[Unit]
}

object Handler {
  def apply[F[_]](implicit ev: Handler[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: WithRun[*[_], I, AppContext]
  ](handlerConfig: HandlerConfig, schedulerConfig: SchedulerConfig)(
    handle: Handle[F],
    sourcesOfCommands: List[Stream[I, HandlerCommand]],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], HandlerEvent],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], HandlerCommand],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], HandlerCommand]
  ) extends Handler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(handlerConfig.kafkaConsumer.maxConcurrentPerPartition) { committable =>
            runContext(handle.handle(committable.record.value))(AppContext()).map(_.toOption -> committable.offset)
          }
          .collect { case (eventOption, offset) =>
            val events  = List(eventOption).flatten
            val records = events.map(event => ProducerRecord(handlerConfig.kafkaProducer.topic, event.key, event))

            ProducerRecords(records, offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(handlerConfig.kafkaProducer.maxBufferSize)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(handlerConfig.kafkaConsumer.commitEveryNOffsets, handlerConfig.kafkaConsumer.commitTimeWindow))
      }.parJoinUnbounded

    def schedule: Stream[I, Unit] =
      Stream
        .emits(sourcesOfCommands)
        .parJoinUnbounded
        .map(command => ProducerRecord(schedulerConfig.kafkaProducer.topic, command.key, command))
        .evalMap(record => producerOfCommands.produce(ProducerRecords.one(record)))
        .parEvalMap(schedulerConfig.kafkaProducer.maxBufferSize)(identity)
        .map(_.passthrough)
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: WithRun[*[_], I, AppContext],
    S[_]: LiftStream[*[_], I]
  ](handlerConfig: HandlerConfig, schedulerConfig: SchedulerConfig)(
    handle: Handle[F],
    sourcesOfCommands: List[Stream[I, HandlerCommand]],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], HandlerEvent],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], HandlerCommand],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], HandlerCommand]
  ): Resource[I, Handler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Handler[Stream[I, *]] =
            new Impl[I, F](handlerConfig, schedulerConfig)(handle, sourcesOfCommands, producerOfEvents, producerOfCommands, consumerOfCommands)

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
      case SourceConfig.OzonCategory(rootCategoryId, every) =>
        Stream
          .awakeEvery[F](every)
          .flatMap { _ =>
            ozonApi
              .getCategories(rootCategoryId)(_.isLeaf)
              .parEvalMapUnordered(128)(leaf => ozonApi.getCategorySearchResultsV2(leaf.id, 1 @@ ozon.Url.Page).map(leaf -> _))
              .flatMap { case (leafCategory, searchResultsV2Option) =>
                searchResultsV2Option
                  .fold[Stream[F, Int]](Stream.range(1, 50)) {
                    _ match {
                      case ozon.SearchResultsV2.Failure(error)      => Stream.empty
                      case ozon.SearchResultsV2.Success(_, page, _) => Stream.range(1, page.total)
                    }
                  }
                  .parEvalMapUnordered(1000) { p =>
                    HandlerCommand.handleOzonRequest[F](
                      ozon.Request.GetCategorySearchResultsV2(leafCategory.id, leafCategory.name, p @@ ozon.Url.Page)
                    )
                  }
              }
          }
    }
}
