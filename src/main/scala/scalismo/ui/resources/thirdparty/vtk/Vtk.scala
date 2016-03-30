package scalismo.ui.resources.thirdparty.vtk

import scalismo.ui.resources.thirdparty.ThirdPartyResource

object Vtk extends ThirdPartyResource {
  override def authors: String = "Kitware, Inc."

  override def name: String = "VTK"

  override def homepage: Option[String] = Some("http://www.vtk.org")

  override def licenseName: String = "BSD"
}
