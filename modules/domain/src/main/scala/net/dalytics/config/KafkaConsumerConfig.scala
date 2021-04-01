package net.dalytics.config

import scala.concurrent.duration._

import cats.effect.{Blocker, ContextShift, Sync}
import derevo.derive
import derevo.pureconfig.pureconfigReader
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

@derive(pureconfigReader)
final case class KafkaConsumerConfig(
  groupId: String,
  topic: String,
  enableAutoCommit: Option[Boolean],
  fetchMaxBytes: Option[Long],             // The maximum amount of data the server should return for a fetch request.
  maxPartitionFetchBytes: Option[Long],    // The maximum amount of data per-partition the server will return.
  maxPollRecords: Option[Int],             // The maximum number of records returned in a single call to poll().
  maxPollInterval: Option[FiniteDuration], // The maximum delay between invocations of poll() when using consumer group management.
  commitTimeout: Option[FiniteDuration],   // FS2 Kafka. The timeout for offset commits.
  commitTimeWindow: FiniteDuration,        // FS2 Kafka. Commits offsets in batches of every `commitEveryNOffsets` offsets or
  commitEveryNOffsets: Int,                //            time window of length `commitTimeWindow`, whichever happens first.
  maxConcurrentPerTopic: Int
)

object KafkaConsumerConfig {
  lazy val load: KafkaConsumerConfig = ConfigSource.default.at("kafka-consumer").loadOrThrow[KafkaConsumerConfig]

  def loadF[F[_]: Sync: ContextShift](implicit blocker: Blocker): F[KafkaConsumerConfig] =
    ConfigSource.default.at("kafka-consumer").loadF[F, KafkaConsumerConfig](blocker)
}
