package scalismo.ui.swing.actions.scenetree

import scalismo.ui.{ StatusMessage, Status, Reloadable, SceneTreeObject }

import scala.util.{ Failure, Success }

class ReloadReloadableAction extends SceneTreePopupAction("Reload") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(rld: Reloadable) => rld.isCurrentlyReloadable
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      context.get.asInstanceOf[Reloadable].reload() match {
        case Success(()) => Status.set(s"Reloaded ${context.get.name}")
        case Failure(ex) => Status.set(StatusMessage(s"Reloading ${context.get.name} failed", StatusMessage.Error, highPriority = true))
      }
    }
  }
}
