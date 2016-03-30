package scalismo.ui.resources.thirdparty.niftijio

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object NiftiJio extends ThirdPartyResource {
  override def authors: String = "Ryan Cabeen"

  override def licenseName: String = "MIT"

  override def name: String = "niftijio"

  override def homepage: Option[String] = Some("https://github.com/cabeen/niftijio")
}
