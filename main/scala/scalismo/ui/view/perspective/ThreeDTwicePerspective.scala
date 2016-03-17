package scalismo.ui.view.perspective

import scalismo.ui.view.{ ScalismoFrame, ViewportPanel, ViewportPanel3D }

import scala.swing.GridPanel

class ThreeDTwicePerspective(override val frame: ScalismoFrame, override val factory: PerspectiveFactory) extends GridPanel(1, 2) with Perspective {
  override val viewports: List[ViewportPanel] = List(new ViewportPanel3D(frame, "Left"), new ViewportPanel3D(frame, "Right"))

  contents ++= viewports
}

object ThreeDTwicePerspective extends PerspectiveFactory {
  override def instantiate(frame: ScalismoFrame): Perspective = new ThreeDTwicePerspective(frame, this)

  override val perspectiveName: String = "Two 3D viewports"
}
