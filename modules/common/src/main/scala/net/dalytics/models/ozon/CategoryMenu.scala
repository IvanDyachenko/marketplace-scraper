package net.dalytics.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import tethys.JsonReader
import io.circe.{Decoder, HCursor}

@derive(loggable)
final case class CategoryMenu(categories: List[Category]) {
  def category(id: Category.Id): Option[Category] =
    categories.foldLeft[Option[Category]](None)((result, category) => result.orElse(category.find(id)))
}

object CategoryMenu {
  implicit def circeDecoder(component: Component.CategoryMenu): Decoder[CategoryMenu] = Decoder.instance[CategoryMenu] { (c: HCursor) =>
    c.downField(component.stateId)
      .get[List[Category]]("categories")(Decoder.decodeList[Category])
      .map(apply)
  }

  implicit def tethysJsonReader(component: Component.CategoryMenu): JsonReader[CategoryMenu] =
    JsonReader.builder
      .addField[List[Category]](component.stateId, JsonReader.builder.addField[List[Category]]("categories").buildReader(identity))
      .buildReader(apply)
}
