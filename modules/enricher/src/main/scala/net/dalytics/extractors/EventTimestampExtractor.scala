package net.dalytics.extractors

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor

import net.dalytics.models.parser.ParserEvent
import net.dalytics.models.enricher.EnricherEvent

class EventTimestampExtractor extends TimestampExtractor {
  def extract(record: ConsumerRecord[AnyRef, AnyRef], previousTimestamp: Long): Long =
    record.value match {
      case event: ParserEvent   => event.timestamp.toEpochMilli
      case event: EnricherEvent => event.timestamp.toEpochMilli
      case value                => throw new IllegalArgumentException(s"EventTimestampExtractor cannot recognize the record value ${value}")
    }
}
