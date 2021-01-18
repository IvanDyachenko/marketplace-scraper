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

import marketplace.config.ParserConfig
import marketplace.context.AppContext
import marketplace.services.Parse
import marketplace.models.{ozon, Command}
import marketplace.models.parser.{ParseOzonResponse, ParseYandexMarketResponse}
import marketplace.models.crawler.{CrawlerEvent, OzonRequestHandled, YandexMarketRequestHandled}
import marketplace.models.parser.OzonResponseParsed

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[F[_]](implicit ev: Parser[F]): ev.type = ev

  private final class Impl[I[_]: Monad: Timer: Concurrent, F[_]: WithRun[*[_], I, AppContext]](config: ParserConfig)(
    parse: Parse[F],
//  producerOfParserEvents: KafkaProducer[I, Event.Key, ParserEvent],
    producerOfOzonItems: KafkaProducer[I, String, ozon.Item],
//  consumerOfParserCommands: KafkaConsumer[I, Command.Key, ParserCommand]
    consumerOfCrawlerEvents: KafkaConsumer[I, Command.Key, CrawlerEvent]
  ) extends Parser[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumerOfCrawlerEvents.partitionedStream.map { partition =>
        partition
          .parEvalMap(config.maxConcurrent) { committable =>
            val command = committable.record.value match {
              case OzonRequestHandled(id, key, created, raw)         =>
                ParseOzonResponse(id @@@ Command.Id, key @@@ Command.Key, created, raw)
              case YandexMarketRequestHandled(id, key, created, raw) =>
                ParseYandexMarketResponse(id @@@ Command.Id, key @@@ Command.Key, created, raw)
            }

            runContext(parse.handle(command))(AppContext()).map(_ -> committable.offset)
          }
          .collect { case (OzonResponseParsed(_, _, _, ozon.Result.SearchResultsV2(items)), offset) =>
            ProducerRecords(items.map(ProducerRecord(config.clickhouseOzonItemsTopic, "ozon", _)), offset)
          }
          .evalMap(producerOfOzonItems.produce)
          .parEvalMap(config.maxConcurrent)(identity)
          .map(_.passthrough)
          .through(commitBatchWithin(config.batchOffsets, config.batchTimeWindow))
      }.parJoinUnbounded
  }

  def make[I[_]: Monad: Concurrent: Timer, F[_]: WithRun[*[_], I, AppContext], S[_]: LiftStream[*[_], I]](config: ParserConfig)(
    parse: Parse[F],
//  producerOfParserEvents: KafkaProducer[I, Event.Key, ParserEvent],
    producerOfOzonItems: KafkaProducer[I, String, ozon.Item],
//  consumerOfParserCommands: KafkaConsumer[I, Command.Key, ParserCommand]
    consumerOfCrawlerEvents: KafkaConsumer[I, Command.Key, CrawlerEvent]
  ): Resource[I, Parser[S]] =
    Resource.liftF {
      Stream
        .eval {
          val impl: Parser[Stream[I, *]] = new Impl[I, F](config)(parse, producerOfOzonItems, consumerOfCrawlerEvents)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
