package net.dalytics.models.ozon

import derevo.derive
import tofu.logging.derivation.loggable
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
final case class CategoryMenu(categories: List[Category]) {
  def category(id: Category.Id): Option[Category] =
    categories.foldLeft[Option[Category]](None)((result, category) => result.orElse(category.find(id)))
}

object CategoryMenu {

  implicit val circeDecoder: Decoder[CategoryMenu] = Decoder.instance[CategoryMenu] { (c: HCursor) =>
    for {
      layout       <- c.get[Layout]("layout")
      categoryMenu <- layout.categoryMenu.fold[Decoder.Result[CategoryMenu]](
                        Left(DecodingFailure("\"layout\" object doesn't contain component which is corresponds to \"categoryMenu\"", c.history))
                      ) { component =>
                        c.downField("catalog")
                          .downField("categoryMenu")
                          .downField(component.stateId)
                          .get[List[Category]]("categories")(Decoder.decodeList[Category])
                          .map(CategoryMenu.apply)
                      }
    } yield categoryMenu
  }
}
