package marketplace.modules

import cats.tagless.syntax.functorK._
import tofu.syntax.embed._
import tofu.syntax.monadic._
import tofu.syntax.context._
import derevo.derive
import cats.Monad
import cats.effect.{Concurrent, Resource, Timer}
import tofu.WithRun
import tofu.higherKind.derived.representableK
import tofu.fs2.LiftStream
import fs2.Stream
import fs2.kafka.{commitBatchWithin, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}

import marketplace.config.CrawlerConfig
import marketplace.context.AppContext
import marketplace.services.Crawl
import marketplace.models.{Command, Event}
import marketplace.models.crawler.{CrawlerCommand, CrawlerEvent}

@derive(representableK)
trait Crawler[S[_]] {
  def run: S[Unit]
}

object Crawler {

  def apply[F[_]](implicit ev: Crawler[F]): ev.type = ev

  def make[I[_]: Monad: Concurrent: Timer, F[_]: WithRun[*[_], I, AppContext], S[_]: LiftStream[*[_], I]](config: CrawlerConfig)(
    crawl: Crawl[F],
    consumer: KafkaConsumer[I, Command.Key, CrawlerCommand],
    producer: KafkaProducer[I, Event.Key, CrawlerEvent]
  ): Resource[I, Crawler[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Crawler[Stream[I, *]] = new Crawler[Stream[I, *]] {
            def run: Stream[I, Unit] =
              consumer.partitionedStream.map { partition =>
                partition
                  .parEvalMap(config.maxConcurrent) { committable =>
                    runContext(crawl.handle(committable.record.value))(AppContext()).map(_.map(_ -> committable.offset))
                  }
                  .collect { case Some((event, offset)) =>
                    ProducerRecords.one(ProducerRecord(config.eventsTopic, event.key, event), offset)
                  }
                  .evalMap(producer.produce)
                  .parEvalMap(1000)(identity)
                  .map(_.passthrough)
                  .through(commitBatchWithin(config.batchOffsets, config.batchTimeWindow))
              }.parJoinUnbounded
          }

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
