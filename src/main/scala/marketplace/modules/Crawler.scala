package marketplace.modules

import cats.effect.Resource
import tofu.WithRun
import derevo.derive
import tofu.higherKind.derived.representableK
import fs2.kafka.{KafkaConsumer, KafkaProducer}

import marketplace.config.CrawlerConfig
import marketplace.context.AppContext
import marketplace.services.Crawl
import marketplace.models.{CommandId, EventId}
import marketplace.models.crawler.{Command, Event}

@derive(representableK)
trait Crawler[F[_]] {
  def run: F[Unit]
}

object Crawler {

  def apply[F[_]](implicit ev: Crawler[F]): ev.type = ev

  def make[I[_], F[_]: WithRun[*[_], I, AppContext]](
    crawl: Crawl[F],
    consumer: KafkaConsumer[I, CommandId, Command],
    producer: KafkaProducer[I, EventId, Event]
  )(
    crawlerConfig: CrawlerConfig
  ): Resource[I, Crawler[I]] = ???
}
