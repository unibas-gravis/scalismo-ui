package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.Removeable
import scalismo.ui.view.ScalismoFrame

object RemoveRemoveablesAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    val nodes = allOf[Removeable](context)
    if (nodes.isEmpty) Nil else List(new RemoveRemoveablesAction(nodes))
  }
}

class RemoveRemoveablesAction(nodes: List[Removeable]) extends PopupAction("Remove") {
  override def apply(): Unit = {
    nodes.foreach(_.remove())
  }
}
