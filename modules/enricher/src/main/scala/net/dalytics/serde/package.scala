package net.dalytics

import compstak.kafkastreams4s.STable
import org.apache.kafka.streams.kstream.KTable

package object serde {
  type VulcanTable[K, V] = STable[VulcanSerdeCodec, K, VulcanSerdeCodec, V]

  object VulcanTable {
    def apply[K: VulcanSerdeCodec, V: VulcanSerdeCodec](kTable: KTable[K, V]): VulcanTable[K, V] =
      new VulcanTable[K, V](kTable)
  }
}
