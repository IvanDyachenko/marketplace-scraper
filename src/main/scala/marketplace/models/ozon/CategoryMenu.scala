package marketplace.models.ozon

import derevo.derive
import tofu.logging.derivation.{loggable, masked, MaskMode}
import io.circe.{Decoder, DecodingFailure, HCursor}

@derive(loggable)
final case class CategoryMenu(@masked(MaskMode.ForLength(0, 50)) categories: List[Category])

object CategoryMenu {

  implicit val circeDecoder: Decoder[CategoryMenu] = Decoder.instance[CategoryMenu] { (c: HCursor) =>
    for {
      layout       <- c.get[Layout]("layout")
      categoryMenu <- layout.categoryMenu.fold[Decoder.Result[CategoryMenu]](
                        Left(
                          DecodingFailure(
                            "\"layout\" object doesn't contain component with \"component\" which is corresponds to \"categoryMenu\"",
                            c.history
                          )
                        )
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
