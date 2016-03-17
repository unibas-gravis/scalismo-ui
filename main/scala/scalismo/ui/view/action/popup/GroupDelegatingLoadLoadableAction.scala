package scalismo.ui.view.action.popup

import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.view.ScalismoFrame

object GroupDelegatingLoadLoadableAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[GroupNode](context).toList.flatMap { group =>
      group.children.flatMap { child => PopupAction(List(child)) }.collect { case p: LoadLoadableAction => p }
    }
  }
}
