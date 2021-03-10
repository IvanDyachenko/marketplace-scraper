package net.dalytics

import cats.tagless.syntax.functorK._
import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import tofu.syntax.embed._
import tofu.syntax.monadic._
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.WithRun
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{KafkaProducer, ProducerRecord, ProducerRecords}
import supertagged.postfix._

import net.dalytics.config.{Config, SourceConfig}
import net.dalytics.context.MessageContext
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
    I[_]: Monad: Concurrent,
    F[_]: WithRun[*[_], I, MessageContext]
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
    F[_]: WithRun[*[_], I, MessageContext],
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    sourcesOfCommands: List[Stream[I, HandlerCommand]],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], HandlerCommand]
  ): Resource[I, Scheduler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Scheduler[Stream[I, *]] = new Impl[I, F](config)(sourcesOfCommands, producerOfCommands)

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
              .getCategories(rootCategoryId)(_.isLeaf)
              .parEvalMapUnordered(128)(leaf => ozonApi.getCategorySearchResultsV2(leaf.id, 1 @@ ozon.Url.Page).map(leaf -> _))
              .flatMap { case (leafCategory, searchResultsV2Option) =>
                searchResultsV2Option
                  .fold[Stream[F, Int]](Stream.range(1, 50)) {
                    _ match {
                      case ozon.SearchResultsV2.Failure(error)      => Stream.empty
                      case ozon.SearchResultsV2.Success(_, page, _) => Stream.range(1, page.total + 1)
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
