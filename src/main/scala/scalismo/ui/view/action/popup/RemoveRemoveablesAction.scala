package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.Removeable
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

object RemoveRemoveablesAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    val nodes = allMatch[Removeable](context)
    if (nodes.isEmpty) Nil else List(new RemoveRemoveablesAction(nodes))
  }
}

class RemoveRemoveablesAction(nodes: List[Removeable], description: String = "Remove") extends PopupAction(description, BundledIcon.Remove) {
  override def apply(): Unit = {
    nodes.foreach(_.remove())
  }
}
