package scalismo.ui.view.perspective

import scalismo.ui.view.{ ScalismoFrame, ViewportPanel, ViewportPanel3D }

import scala.swing.BorderPanel

class ThreeDOnlyPerspective(override val frame: ScalismoFrame, override val factory: PerspectiveFactory) extends BorderPanel with Perspective {
  val viewport = new ViewportPanel3D(frame)

  override val viewports: List[ViewportPanel] = List(viewport)

  layout(viewport) = BorderPanel.Position.Center
}

object ThreeDOnlyPerspective extends PerspectiveFactory {
  override def instantiate(frame: ScalismoFrame): Perspective = new ThreeDOnlyPerspective(frame, this)

  override val perspectiveName: String = "Single 3D viewport"
}