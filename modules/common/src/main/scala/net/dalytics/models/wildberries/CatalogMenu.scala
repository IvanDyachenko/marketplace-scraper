package net.dalytics.models.wildberries

import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.{Decoder, HCursor}

@derive(loggable)
final case class CatalogMenu(@masked(MaskMode.ForLength(0, 50)) catalogs: List[Catalog]) {
  def catalog(id: Catalog.Id): Option[Catalog] = catalogs.foldLeft[Option[Catalog]](None)((result, catalog) => result.orElse(catalog.find(id)))
}

object CatalogMenu {
  implicit val circeDecoder: Decoder[CatalogMenu] = Decoder.instance[CatalogMenu] { (c: HCursor) =>
    c.downField("data")
      .get[List[Catalog]]("catalog")(Decoder.decodeList[Catalog])
      .map(apply)
  }
}
