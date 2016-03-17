
package scalismo.ui.view.action.popup

import scalismo.ui.model.SceneNode
import scalismo.ui.model.capabilities.Renameable
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.AskForInputAction

object RenameRenameableAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[Renameable](context).map(n => new RenameRenameableAction(n)).toList
  }
}

class RenameRenameableAction(node: Renameable)(implicit val frame: ScalismoFrame) extends PopupAction("Rename ...", BundledIcon.Name) {
  def callback(newName: Option[String]): Unit = {
    newName.foreach(node.name = _)
  }

  override def apply(): Unit = {
    new AskForInputAction[String](s"Rename ${node.name} to:", node.name, callback, "Rename").apply()
  }
}
