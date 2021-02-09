package marketplace.modules

import cats.tagless.syntax.functorK._
import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import tofu.syntax.handle._
import derevo.derive
import tofu.higherKind.derived.representableK
import tofu.WithRun
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{commitBatchWithin, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}
import tofu.generate.GenUUID
import supertagged.postfix._

import marketplace.config.{CrawlerConfig, SourceConfig}
import marketplace.context.AppContext
import marketplace.api.OzonApi
import marketplace.clients.HttpClient
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
  ](config: CrawlerConfig)(
    crawl: Crawl[F],
    sourcesOfCrawlerCommands: List[Stream[I, CrawlerCommand]],
    producerOfCrawlerEvents: KafkaProducer[I, Event.Key, CrawlerEvent],
    producerOfCrawlerCommands: KafkaProducer[I, Command.Key, CrawlerCommand],
    consumerOfCrawlerCommands: KafkaConsumer[I, Command.Key, CrawlerCommand]
  ) extends Crawler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCrawlerCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.maxConcurrent) { committable =>
            runContext(crawl.handle(committable.record.value))(AppContext()).map(_.toOption.map(_ -> committable.offset))
          }
          .collect { case Some((event, offset)) =>
            ProducerRecords.one(ProducerRecord(config.eventsTopic, event.key, event), offset)
          }
          .evalMap(producerOfCrawlerEvents.produce)
          .parEvalMap(config.maxConcurrent)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.batchOffsets, config.batchTimeWindow))
      }.parJoinUnbounded

    def schedule: Stream[I, Unit] =
      Stream
        .emits(sourcesOfCrawlerCommands)
        .parJoinUnbounded
        .evalMap(command => producerOfCrawlerCommands.produce(ProducerRecords.one(ProducerRecord(config.commandsTopic, command.key, command))))
        .parEvalMap(1000)(identity)
        .map(_.passthrough)
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: WithRun[*[_], I, AppContext],
    S[_]: LiftStream[*[_], I]
  ](config: CrawlerConfig)(
    crawl: Crawl[F],
    sourcesOfCrawlerCommands: List[Stream[I, CrawlerCommand]],
    producerOfCrawlerEvents: KafkaProducer[I, Event.Key, CrawlerEvent],
    producerOfCrawlerCommands: KafkaProducer[I, Command.Key, CrawlerCommand],
    consumerOfCrawlerCommands: KafkaConsumer[I, Command.Key, CrawlerCommand]
  ): Resource[I, Crawler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Crawler[Stream[I, *]] =
            new Impl[I, F](config)(crawl, sourcesOfCrawlerCommands, producerOfCrawlerEvents, producerOfCrawlerCommands, consumerOfCrawlerCommands)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }

  def makeCrawlerCommandsSource[F[_]: Timer: Concurrent: GenUUID: HttpClient.Handling](sourceConfig: SourceConfig)(
    ozonApi: OzonApi[F, Stream[F, *]]
  ): Stream[F, CrawlerCommand] =
    sourceConfig match {
      case SourceConfig.OzonCategory(rootCategoryId, every) =>
        Stream.awakeEvery[F](every) >>= { _ =>
          for {
            leafCategory    <- ozonApi.getCategories(rootCategoryId)(_.isLeaf)
            searchResultsV2 <- Stream.eval(ozonApi.getCategorySearchResultsV2(leafCategory.id, 1 @@ ozon.Url.Page).restore)
            crawlerCommands <- searchResultsV2.fold[Stream[F, CrawlerCommand]](Stream.empty) {
                                 _ match {
                                   case ozon.SearchResultsV2.Failure(_)                                      => Stream.empty
                                   case ozon.SearchResultsV2.Success(ozon.Category(id, name, _, _), page, _) =>
                                     Stream
                                       .emits(1 to page.total)
                                       .covary[F]
                                       .parEvalMapUnordered(1000) { number =>
                                         CrawlerCommand.handleOzonRequest[F](
                                           ozon.Request.GetCategorySearchResultsV2(id, name, number @@ ozon.Url.Page)
                                         )
                                       }
                                 }
                               }
          } yield crawlerCommands
        }
    }
}
