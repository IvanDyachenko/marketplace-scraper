package marketplace.clients.models

import cats.Inject
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.circe.parser.decode
import java.nio.charset.StandardCharsets.UTF_8

package object schema {

  // https://github.com/cr-org/neutron/blob/master/circe/src/main/scala/cr/pulsar/schema/circe.scala
  implicit def circeBytesInject[T: Encoder: Decoder]: Inject[T, Array[Byte]] =
    new Inject[T, Array[Byte]] {
      val inj: T => Array[Byte]         = _.asJson.noSpaces.getBytes(UTF_8)
      val prj: Array[Byte] => Option[T] = bytes => decode[T](new String(bytes, UTF_8)).toOption
    }
}
