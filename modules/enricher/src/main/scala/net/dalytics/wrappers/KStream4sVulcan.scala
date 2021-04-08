package net.dalytics.wrappers

import scala.jdk.CollectionConverters._

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed

import net.dalytics.serdes.VulcanSerdeCodec

package object vulcan {
  type KStream4sVulcan[K, V] = KStream4s[VulcanSerdeCodec, K, VulcanSerdeCodec, V]

  object KStream4sVulcan {
    def apply[K: VulcanSerdeCodec, V: VulcanSerdeCodec](streamsBuilder: StreamsBuilder, sourceTopic: String): KStream4sVulcan[K, V] =
      KStream4s.fromKStream(
        streamsBuilder.stream(sourceTopic, Consumed.`with`(VulcanSerdeCodec[K].serde, VulcanSerdeCodec[V].serde))
      )

    def apply[K: VulcanSerdeCodec, V: VulcanSerdeCodec](streamsBuilder: StreamsBuilder, sourceTopics: List[String]): KStream4sVulcan[K, V] =
      KStream4s.fromKStream(
        streamsBuilder.stream(sourceTopics.asJava, Consumed.`with`(VulcanSerdeCodec[K].serde, VulcanSerdeCodec[V].serde))
      )
  }
}
