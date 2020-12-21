package marketplace.modules

import derevo.derive
import tofu.higherKind.derived.representableK

@derive(representableK)
trait Parser[S[_]] {
  def run: S[Unit]
}

object Parser {
  def apply[S[_]](implicit ev: Parser[S]): ev.type = ev
}
