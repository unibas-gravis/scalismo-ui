package scalismo.ui.swing.actions.scenetree

import scalismo.ui.{ Removeable, RemoveableChildren, SceneTreeObject }

class RemoveRemoveableAction extends SceneTreePopupAction("Remove") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(r: Removeable) =>
        title = "Remove"
        r.isCurrentlyRemoveable
      case Some(r: RemoveableChildren) =>
        title = "Remove all"
        r.children.foldLeft(false) {
          case (b, c) => c.asInstanceOf[Removeable].isCurrentlyRemoveable || b
        }
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    context match {
      case Some(r: Removeable) => r.remove()
      case Some(r: RemoveableChildren) => r.removeAll()
      case _ =>
    }
  }
}
