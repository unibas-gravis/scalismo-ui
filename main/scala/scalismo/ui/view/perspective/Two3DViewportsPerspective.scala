package scalismo.ui.view.perspective

import scalismo.ui.view.{ ViewportPanel3D, ScalismoFrame, ViewportPanel }

import scala.swing.GridPanel

class Two3DViewportsPerspective(override val frame: ScalismoFrame, override val factory: PerspectiveFactory) extends GridPanel(1, 2) with Perspective {
  override val viewports: List[ViewportPanel] = List(new ViewportPanel3D(frame, "Left"), new ViewportPanel3D(frame, "Right"))

  contents ++= viewports
}

object Two3DViewportsPerspective extends PerspectiveFactory {
  override def instantiate(frame: ScalismoFrame): Perspective = new Two3DViewportsPerspective(frame, this)

  override val perspectiveName: String = "Two 3D viewports"
}
