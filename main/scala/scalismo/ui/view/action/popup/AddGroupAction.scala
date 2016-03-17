
package scalismo.ui.view.action.popup

import scalismo.ui.model.{ Scene, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.AskForInputAction

object AddGroupAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[Scene](context).map(n => new AddGroupAction(n)).toList
  }
}

class AddGroupAction(node: Scene)(implicit val frame: ScalismoFrame) extends PopupAction("Add Group ...", BundledIcon.Group) {
  def callback(newName: Option[String]): Unit = {
    newName.foreach(node.groups.add)
  }

  override def apply(): Unit = {
    new AskForInputAction[String](s"Enter a name for the new group:", "", callback, "Add Group").apply()
  }
}
