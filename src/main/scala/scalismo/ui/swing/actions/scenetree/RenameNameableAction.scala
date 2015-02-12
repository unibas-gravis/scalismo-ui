package scalismo.ui.swing.actions.scenetree

import scalismo.ui.{ Nameable, SceneTreeObject }

import scala.swing.Dialog

class RenameNameableAction extends SceneTreePopupAction("Rename...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isNameUserModifiable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val nameable = context.get.asInstanceOf[Nameable]
      val newNameOpt = Dialog.showInput[String](title = "Rename", message = "Rename \"" + nameable.name + "\" to:", initial = nameable.name)
      newNameOpt.map(s => nameable.name = s)
    }
  }
}
