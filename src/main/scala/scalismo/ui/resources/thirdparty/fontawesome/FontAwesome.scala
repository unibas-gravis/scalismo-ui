package scalismo.ui.resources.thirdparty.fontawesome

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object FontAwesome extends ThirdPartyResource {
  override def name: String = "Font Awesome"

  override def licenseName: String = "SIL OFL 1.1"

  override def authors: String = "Dave Gandy"

  override def homepage: Option[String] = Some("http://fontawesome.io")
}
