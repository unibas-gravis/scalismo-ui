package scalismo.ui.swing.actions.scenetree

import scalismo.ui.{ Reloadable, SceneTreeObject }

class ReloadReloadableAction extends SceneTreePopupAction("Reload") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(rld: Reloadable) => rld.isCurrentlyReloadable
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      context.get.asInstanceOf[Reloadable].reload()
    }
  }
}
