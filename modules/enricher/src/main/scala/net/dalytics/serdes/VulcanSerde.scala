package net.dalytics.serdes

import java.nio.ByteBuffer

import tofu.syntax.monadic._
import cats.FlatMap
import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}
import io.confluent.kafka.schemaregistry.avro.AvroSchema
import fs2.kafka.vulcan.AvroSettings
import vulcan.{Codec => VulcanCodec}

final class VulcanSerde[A](private val codec: VulcanCodec[A]) extends AnyVal {

  /** ToDo:
    *   UnliftIO
    *   avroSerializer[K].using(avroSettings).forKey/foValue
    *   avroDeserializer[K].using(avroSettings).forKey/forValue
    *
    * @param avroSettings
    * @param isKey
    * @return
    */
  def using[F[_]: FlatMap](avroSettings: AvroSettings[F])(isKey: Boolean): F[Serde[A]] = {
    val createSerializer: Boolean => F[Serializer[A]] =
      avroSettings.createAvroSerializer(_).map { case (kafkaAvroSerializer, _) =>
        new Serializer[A] {
          def serialize(topic: String, data: A): Array[Byte] = codec.encode(data) match {
            case Right(value) => kafkaAvroSerializer.serialize(topic, value)
            case Left(error)  => throw error.throwable
          }
        }
      }

    val createDeserializer: Boolean => F[Deserializer[A]] =
      codec.schema match {
        case Right(schema) =>
          avroSettings.createAvroDeserializer(_).map { case (kafkaAvroDeserializer, schemaRegistryClient) =>
            new Deserializer[A] {
              def deserialize(topic: String, bytes: Array[Byte]): A = {
                val writerSchemaId = ByteBuffer.wrap(bytes).getInt(1) // skip magic byte

                val writerSchema = {
                  val schema = schemaRegistryClient.getSchemaById(writerSchemaId)
                  if (schema.isInstanceOf[AvroSchema]) schema.asInstanceOf[AvroSchema].rawSchema()
                  else null
                }

                codec.decode(kafkaAvroDeserializer.deserialize(topic, bytes, schema), writerSchema) match {
                  case Right(a)    => a
                  case Left(error) => throw error.throwable
                }
              }
            }
          }
        case Left(error)   => throw error.throwable
      }

    for {
      avroSerializer   <- createSerializer(isKey)
      avroDeserializer <- createDeserializer(isKey)
      serde             = new Serde[A] {
                            def serializer(): Serializer[A]     = avroSerializer
                            def deserializer(): Deserializer[A] = avroDeserializer
                          }
    } yield serde
  }
}

object VulcanSerde {
  def apply[A](implicit codec: VulcanCodec[A]): VulcanSerde[A] = new VulcanSerde[A](codec)
}
