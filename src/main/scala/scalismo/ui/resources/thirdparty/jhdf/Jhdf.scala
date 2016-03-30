package scalismo.ui.resources.thirdparty.jhdf

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Jhdf extends ThirdPartyResource {
  override def authors: String = "ETH Zürich"

  override def licenseName: String = "Apache 2.0"

  override def name: String = "JHDF5"

  override def homepage: Option[String] = Some("https://wiki-bsse.ethz.ch/display/JHDF5")
}
