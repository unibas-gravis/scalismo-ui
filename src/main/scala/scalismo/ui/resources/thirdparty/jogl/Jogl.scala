package scalismo.ui.resources.thirdparty.jogl

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Jogl extends ThirdPartyResource {
  override def authors: String = "JogAmp"

  override def licenseName: String = "New BSD 2-Clause"

  override def name: String = "JOGL"

  override def homepage: Option[String] = Some("http://jogamp.org/jogl")
}
