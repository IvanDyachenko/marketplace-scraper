package net.dalytics.config

import scala.concurrent.duration._

import cats.effect.Sync
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaStreamsConfig(
  applicationId: String,
  closeTimeout: FiniteDuration,
  sourceTopic: String,
  sinkTopic: String,
  numberOfStreamThreads: Int,     // The number of threads to execute stream processing.
  commitInterval: FiniteDuration, // The frequency in milliseconds with which to save the position of the processor.
  cacheMaxBytesBuffering: Long,   // Maximum number of memory bytes to be used for buffering across all threads.
  fetchMaxBytes: Long,            // The maximum amount of data the server should return for a fetch request.
  maxPartitionFetchBytes: Long,   // The maximum amount of data per-partition the server will return.
  maxPollRecords: Int,            // The maximum number of records returned in a single call to poll().
  bufferMemory: Long,             // The total bytes of memory the producer can use to buffer records waiting to be sent to the server.
  compressionType: String,
  linger: FiniteDuration
)

object KafkaStreamsConfig {
  lazy val load: KafkaStreamsConfig = ConfigSource.default.at("kafka-streams").loadOrThrow[KafkaStreamsConfig]

  def loadF[F[_]: Sync: ContextShift]: F[KafkaStreamsConfig] =
    ConfigSource.default.at("kafka-streams").loadF[F, KafkaStreamsConfig](blocker)
}
