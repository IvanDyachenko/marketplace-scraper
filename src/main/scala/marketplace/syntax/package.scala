package marketplace

package object syntax {

  implicit class StringOps(s: String) {
    def decapitalize: String =
      if (s == null || s.capitalize.length == 0 || s.charAt(0).isLower) s
      else s.updated(0, s.charAt(0).toLower)
  }
}
