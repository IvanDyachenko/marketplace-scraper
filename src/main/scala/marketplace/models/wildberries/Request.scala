package marketplace.models.wildberries

import derevo.derive
import tofu.logging.derivation.loggable

@derive(loggable)
sealed trait Request {
  def host: String
  def path: String
}

object Request {

  @derive(loggable)
  final object GetWildBerriesMenu extends Request {
    val host: String = "wbxmenu.wildberries.ru"
    val path: String = "/v4/api"
  }
}
