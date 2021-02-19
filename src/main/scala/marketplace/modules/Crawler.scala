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
    sourcesOfCommands: List[Stream[I, CrawlerCommand]],
    producerOfEvents: KafkaProducer[I, Option[Event.Key], CrawlerEvent],
    producerOfCommands: KafkaProducer[I, Option[Command.Key], CrawlerCommand],
    consumerOfCommands: KafkaConsumer[I, Option[Command.Key], CrawlerCommand]
  ) extends Crawler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCommands.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.kafkaProducer.maxBufferSize) { committable =>
            runContext(crawl.handle(committable.record.value))(AppContext()).map(_.toOption.map(_ -> committable.offset))
          }
          .collect { case Some((event, offset)) =>
            ProducerRecords.one(ProducerRecord(config.kafkaProducer.topic, event.key, event), offset)
          }
          .evalMap(producerOfEvents.produce)
          .parEvalMap(config.kafkaProducer.maxBufferSize)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.kafkaConsumer.batchOffsets, config.kafkaConsumer.batchTimeWindow))
      }.parJoinUnbounded

    def schedule: Stream[I, Unit] =
      Stream
        .emits(sourcesOfCommands)
        .parJoinUnbounded
        .evalMap(command => producerOfCommands.produce(ProducerRecords.one(ProducerRecord(???, command.key, command))))
        .parEvalMap(1000)(identity)
        .map(_.passthrough)
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: WithRun[*[_], I, AppContext],
    S[_]: LiftStream[*[_], I]
  ](config: CrawlerConfig)(
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
            new Impl[I, F](config)(crawl, sourcesOfCommands, producerOfEvents, producerOfCommands, consumerOfCommands)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }

  def makeCrawlerCommandsSource[F[_]: Timer: Concurrent: HttpClient.Handling](sourceConfig: SourceConfig)(
    ozonApi: OzonApi[F, Stream[F, *]]
  ): Stream[F, CrawlerCommand] =
    sourceConfig match {
      case SourceConfig.OzonCategory(rootCategoryId, every) =>
        Stream.awakeEvery[F](every) >>= { _ =>
          for {
            leafCategoryO    <- ozonApi.getCategories(rootCategoryId)(_.isLeaf).restore
            searchResultsV2O <- leafCategoryO.fold[Stream[F, Option[ozon.SearchResultsV2]]](Stream.empty) { leafCategory =>
                                  Stream.eval(ozonApi.getCategorySearchResultsV2(leafCategory.id, 1 @@ ozon.Url.Page).restore)
                                }
            crawlerCommands  <- searchResultsV2O.fold[Stream[F, CrawlerCommand]](Stream.empty) {
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
