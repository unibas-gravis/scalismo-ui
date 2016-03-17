package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.view.{ ScalismoFrame, ViewportPanel }

object VisibilityAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    val show = new VisibilityAction(context, frame.perspective.viewports, true)
    val hide = new VisibilityAction(context, frame.perspective.viewports, false)
    List(show, hide)
  }
}

class VisibilityAction(nodes: List[SceneNode], viewports: List[ViewportPanel], show: Boolean)(implicit val frame: ScalismoFrame) extends PopupAction("SHOW: " + show) {
  override def apply(): Unit = {

    val control = frame.sceneControl.nodeVisibility

    nodes.foreach { node =>
      control.setNodeVisibility(node, viewports, show)
    }
  }
}
