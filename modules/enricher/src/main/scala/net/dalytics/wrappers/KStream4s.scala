package net.dalytics.wrappers

import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.{KStream, Reducer, ValueMapper}
import compstak.kafkastreams4s.{Codec => Codec4s, SerdeHelpers => Serde4s, STable => KTable4s}

final case class KStream4s[HK[_]: Codec4s, K: HK, HV[_]: Codec4s, V: HV](val toKStream: KStream[K, V]) {
  def collect[V2: HV](pf: PartialFunction[V, V2]): KStream4s[HK, K, HV, V2] =
    mapFilter(pf.lift)

  def reduceByKey(op: (V, V) => V): KTable4s[HK, K, HV, V] =
    KTable4s.fromKTable(
      toKStream
        .groupByKey(Serde4s.groupedForCodec[HK, K, HV, V])
        .reduce(
          ((pv: V, cv: V) => op(pv, cv)): Reducer[V],
          Serde4s.materializedForCodec[HK, K, HV, V]
        )
    )

  def mapFilter[V2: HV](f: V => Option[V2]): KStream4s[HK, K, HV, V2] =
    KStream4s.fromKStream(
      toKStream
        .mapValues(((v: V) => f(v)): ValueMapper[V, Option[V2]])
        .filter((_, ov) => ov.isDefined)
        .mapValues(((ov: Option[V2]) => ov.get): ValueMapper[Option[V2], V2])
    )
}

object KStream4s {
  def apply[HK[_]: Codec4s, K: HK, HV[_]: Codec4s, V: HV](streamsBuilder: StreamsBuilder, sourceTopic: String): KStream4s[HK, K, HV, V] =
    fromKStream(streamsBuilder.stream(sourceTopic, Serde4s.consumedForCodec[HK, K, HV, V]))

  def fromKStream[HK[_]: Codec4s, K: HK, HV[_]: Codec4s, V: HV](kStream: KStream[K, V]): KStream4s[HK, K, HV, V] =
    KStream4s(kStream)
}
