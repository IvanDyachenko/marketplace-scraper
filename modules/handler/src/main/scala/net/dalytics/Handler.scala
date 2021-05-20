package net.dalytics

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
import fs2.kafka.{KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords}

import net.dalytics.config.Config
import net.dalytics.services.Handle
import net.dalytics.context.MessageContext
import net.dalytics.models.handler.{HandlerCommand => Command, HandlerEvent => Event}

@derive(representableK)
trait Handler[S[_]] {
  def run: S[Unit]
}

object Handler {
  def apply[F[_]](implicit ev: Handler[F]): ev.type = ev

  private final class Impl[
    I[_]: Monad: Timer: Concurrent,
    F[_]: WithRun[*[_], I, MessageContext]
  ](config: Config)(
    handle: Handle[F],
    consumer: KafkaConsumer[I, Unit, Command],
    producer: KafkaProducer[I, Unit, Event]
  ) extends Handler[Stream[I, *]] {
    def run: Stream[I, Unit] =
      consumer.partitionsMapStream
        .map { assignments =>
          val numberOfAssignedPartitionsPerTopic = assignments.keySet.groupMapReduce(_.topic)(_ => 1)(_ + _)

          val partitions = assignments.map { case (topicPartition, partition) =>
            val numberOfAssignedPartitions = numberOfAssignedPartitionsPerTopic(topicPartition.topic)
            val maxConcurrentPerPartition  = config.kafkaConsumerConfig.maxConcurrentPerTopic / numberOfAssignedPartitions

            partition
              .parEvalMapUnordered(maxConcurrentPerPartition) { committable =>
                val offset  = committable.offset
                val command = committable.record.value
                val context = MessageContext(
                  consumerGroupId = offset.consumerGroupId,
                  topic = topicPartition.topic,
                  partition = topicPartition.partition,
                  offset = offset.offsetAndMetadata.offset,
                  metadata = offset.offsetAndMetadata.metadata
                )

                runContext(handle.handle(command))(context).map(_.toOption -> offset)
              }
              .collect { case (eventOption, offset) =>
                val events  = List(eventOption).flatten
                val records = events.map { event =>
                  val topic = event match {
                    case _: Event.OzonRequestHandled => config.kafkaProducerConfig.topic("events-ozon")
                  }

                  ProducerRecord(topic, (), event)
                }

                ProducerRecords(records, offset)
              }
              .evalMap(producer.produce)
              .parEvalMap(config.kafkaProducerConfig.parallelism)(identity)
              .void
          //  .map(_.passthrough)
          //  .through(commitBatchWithin(config.kafkaConsumerConfig.commitEveryNOffsets, config.kafkaConsumerConfig.commitTimeWindow))
          }.toList

          Stream.emits(partitions)
        }
        .flatten
        .parJoinUnbounded
  }

  def make[
    I[_]: Monad: Concurrent: Timer,
    F[_]: WithRun[*[_], I, MessageContext],
    S[_]: LiftStream[*[_], I]
  ](config: Config)(
    handle: Handle[F],
    consumer: KafkaConsumer[I, Unit, Command],
    producer: KafkaProducer[I, Unit, Event]
  ): Resource[I, Handler[S]] =
    Resource.eval {
      Stream
        .eval {
          val impl: Handler[Stream[I, *]] = new Impl[I, F](config)(handle, consumer, producer)

          impl.pure[I]
        }
        .embed
        .mapK(LiftStream[S, I].liftF)
        .pure[I]
    }
}
