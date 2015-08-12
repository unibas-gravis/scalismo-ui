package scalismo.ui.swing.actions.scenetree

import scalismo.ui._

class ResetNamesAction extends SceneTreePopupAction("Re-initialize landmark names") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context match {
      case Some(lms: Landmarks[_] with HasNameGenerator) => true
      case _ => false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    context match {
      case Some(lms: Landmarks[_] with HasNameGenerator) =>
        val ng = lms.nameGenerator
        ng.reset()
        lms.children.foreach {
          _.name = ng.nextName
        }
      case _ =>
    }
  }
}
