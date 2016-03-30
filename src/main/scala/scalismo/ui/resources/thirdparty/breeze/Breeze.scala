package scalismo.ui.resources.thirdparty.breeze

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Breeze extends ThirdPartyResource {
  override def authors: String = "David Hall et al."

  override def licenseName: String = "Apache 2.0"

  override def name: String = "Breeze"

  override def homepage: Option[String] = Some("https://github.com/scalanlp/breeze")
}
