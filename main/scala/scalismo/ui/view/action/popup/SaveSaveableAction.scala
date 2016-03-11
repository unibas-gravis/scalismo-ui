package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.Saveable
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.SaveAction

object SaveSaveableAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): Option[PopupAction] = {
    single[Saveable](context).map(n => new SaveSaveableAction(n))
  }
}

class SaveSaveableAction(node: Saveable)(implicit val frame: ScalismoFrame) extends PopupAction("Save...") {
  override def apply(): Unit = new SaveAction(node.save, node.saveMetadata, title)
}
