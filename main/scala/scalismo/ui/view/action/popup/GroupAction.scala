package scalismo.ui.view.action.popup

import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.view.ScalismoFrame

object GroupAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleNode[GroupNode](context).toList.flatMap { group =>
      group.children.flatMap { child => PopupAction.apply(List(child)) }
    }
  }
}
