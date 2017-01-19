package scalismo.ui.resources.thirdparty.spire

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Spire extends ThirdPartyResource {
  override def authors: String = "Erik Osheim, Tom Switzer"

  override def licenseName: String = "MIT"

  override def name: String = "Spire"

  override def homepage: Option[String] = Some("https://github.com/non/spire")
}
