package scalismo.ui.view.perspective

import scalismo.ui.model.Axis
import scalismo.ui.view.{ ViewportPanel2D, ViewportPanel3D, ViewportPanel, ScalismoFrame }

import scala.swing.GridPanel

class OrthogonalPerspective(override val frame: ScalismoFrame, override val factory: PerspectiveFactory) extends GridPanel(2, 2) with Perspective {
  override val viewports: List[ViewportPanel] = List(new ViewportPanel3D(frame), new ViewportPanel2D(frame, Axis.X), new ViewportPanel2D(frame, Axis.Y), new ViewportPanel2D(frame, Axis.Z))
  contents ++= viewports
}

object OrthogonalPerspective extends PerspectiveFactory {
  override def instantiate(frame: ScalismoFrame): Perspective = new OrthogonalPerspective(frame, this)

  override def perspectiveName: String = "3D and Orthogonal Slices"
}
