package scalismo.ui.view.perspective

import scalismo.ui.model.Axis
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel, ViewportPanel2D }

import scala.swing.BorderPanel

object TwoDOnlyPerspective {

  object X extends PerspectiveFactory {
    override def instantiate(frame: ScalismoFrame): Perspective = new TwoDOnlyPerspective(frame, Axis.X, this)

    override val perspectiveName: String = "X Slice"
  }

  object Y extends PerspectiveFactory {
    override def instantiate(frame: ScalismoFrame): Perspective = new TwoDOnlyPerspective(frame, Axis.Y, this)

    override val perspectiveName: String = "Y Slice"
  }

  object Z extends PerspectiveFactory {
    override def instantiate(frame: ScalismoFrame): Perspective = new TwoDOnlyPerspective(frame, Axis.Z, this)

    override val perspectiveName: String = "Z Slice"
  }

}

class TwoDOnlyPerspective(override val frame: ScalismoFrame, axis: Axis, override val factory: PerspectiveFactory) extends BorderPanel with Perspective {
  val viewport = new ViewportPanel2D(frame, axis)

  override val viewports: List[ViewportPanel] = List(viewport)

  layout(viewport) = BorderPanel.Position.Center
}

