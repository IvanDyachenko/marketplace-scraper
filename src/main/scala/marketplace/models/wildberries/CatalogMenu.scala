package marketplace.models.wildberries

import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.{Decoder, HCursor}

@derive(loggable)
final case class CatalogMenu(@masked(MaskMode.ForLength(0, 50)) catalogs: List[Catalog])

object CatalogMenu {

  implicit val circeDecoder: Decoder[CatalogMenu] = Decoder.instance[CatalogMenu] { (c: HCursor) =>
    c.downField("data").get[List[Catalog]]("catalog")(Decoder.decodeList[Catalog]).map(apply)
  }
}
