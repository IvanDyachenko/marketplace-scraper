package marketplace.modules

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

import marketplace.config.{CrawlerConfig, SchedulerConfig, SourceConfig}
import marketplace.context.AppContext
import marketplace.api.OzonApi
import marketplace.services.Crawl
import marketplace.models.{ozon, Command, Event}
import marketplace.models.crawler.{CrawlerCommand, CrawlerEvent}

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
  def schedule: S[Unit]
}

object Crawler {
  def apply[F[_]](implicit ev: Crawler[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: WithRun[*[_], I, AppContext]
  ](crawlerConfig: CrawlerConfig, schedulerConfig: SchedulerConfig)(
    crawl: Crawl[F],
    sourcesOfCommands: List[Stream[I, CrawlerCommand]],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], CrawlerEvent],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], CrawlerCommand],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], CrawlerCommand]
  ) extends Crawler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(crawlerConfig.maxConnectionsPerPartition) { committable =>
            runContext(crawl.handle(committable.record.value))(AppContext()).map(_.toOption -> committable.offset)
          }
          .collect { case (eventOption, offset) =>
            val events  = List(eventOption).flatten
            val records = events.map(event => ProducerRecord(crawlerConfig.kafkaProducer.topic, event.key, event))

            ProducerRecords(records, offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(crawlerConfig.kafkaProducer.maxBufferSize)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(crawlerConfig.kafkaConsumer.commitEveryNOffsets, crawlerConfig.kafkaConsumer.commitTimeWindow))
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
  ](crawlerConfig: CrawlerConfig, schedulerConfig: SchedulerConfig)(
    crawl: Crawl[F],
    sourcesOfCommands: List[Stream[I, CrawlerCommand]],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], CrawlerEvent],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], CrawlerCommand],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], CrawlerCommand]
  ): Resource[I, Crawler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Crawler[Stream[I, *]] =
            new Impl[I, F](crawlerConfig, schedulerConfig)(crawl, sourcesOfCommands, producerOfEvents, producerOfCommands, consumerOfCommands)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }

  def makeCommandsSource[F[_]: Timer: Concurrent](sourceConfig: SourceConfig)(
    ozonApi: OzonApi[F, Stream[F, *]]
  ): Stream[F, CrawlerCommand] =
    sourceConfig match {
      case SourceConfig.OzonCategory(rootCategoryId, every) =>
        Stream.awakeEvery[F](every) >>= { _ =>
          ozonApi.getCategories(rootCategoryId)(_.isLeaf) >>= { leafCategory =>
            Stream.eval(ozonApi.getCategorySearchResultsV2(leafCategory.id, 1 @@ ozon.Url.Page)) >>= { searchResultsV2Option =>
              searchResultsV2Option
                .fold[Stream[F, Int]](Stream.range(1, 50)) {
                  _ match {
                    case ozon.SearchResultsV2.Failure(_)          => Stream.empty
                    case ozon.SearchResultsV2.Success(_, page, _) => Stream.range(1, page.total)
                  }
                }
                .parEvalMapUnordered(100) { p =>
                  CrawlerCommand.handleOzonRequest[F](ozon.Request.GetCategorySearchResultsV2(leafCategory.id, leafCategory.name, p @@ ozon.Url.Page))
                }
            }
          }
        }
    }
}
