package net.dalytics.serdes

import compstak.kafkastreams4s.{Codec => KafkaStreams4sCodec}
import org.apache.kafka.common.serialization.Serde
import vulcan.{Codec => VulcanCodec}
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.Deserializer

trait VulcanSerdeCodec[A] {
  def serde: Serde[A]
  def vulcanCodec: VulcanCodec[A]
}

object VulcanSerdeCodec {

  implicit val kafkaStreams4sCodec: KafkaStreams4sCodec[VulcanSerdeCodec] =
    new KafkaStreams4sCodec[VulcanSerdeCodec] { self =>
      def serde[A](implicit vulcanSerdeCodec: VulcanSerdeCodec[A]): Serde[A] = vulcanSerdeCodec.serde

      def optionSerde[A](implicit vulcanSerdeCodec: VulcanSerdeCodec[A]): VulcanSerdeCodec[Option[A]] =
        new VulcanSerdeCodec[Option[A]] {
          def serde: Serde[Option[A]] = new Serde[Option[A]] {
            def serializer(): Serializer[Option[A]]     = new Serializer[Option[A]] {
              def serialize(topic: String, data: Option[A]): Array[Byte] =
                data match {
                  case Some(data) => self.serde.serializer.serialize(topic, data)
                  case None       => null
                }
            }
            def deserializer(): Deserializer[Option[A]] = new Deserializer[Option[A]] {
              def deserialize(topic: String, bytes: Array[Byte]): Option[A] =
                if (bytes == null)
                  None
                else
                  Some(self.serde.deserializer.deserialize(topic, bytes))
            }
          }

          def vulcanCodec: VulcanCodec[Option[A]] = VulcanCodec.option[A](vulcanSerdeCodec.vulcanCodec)
        }
    }

  implicit def vulcanSerdeCodec[A](implicit _serde: Serde[A], _vulcanCodec: VulcanCodec[A]): VulcanSerdeCodec[A] =
    new VulcanSerdeCodec[A] {
      def serde: Serde[A]             = _serde
      def vulcanCodec: VulcanCodec[A] = _vulcanCodec
    }
}
