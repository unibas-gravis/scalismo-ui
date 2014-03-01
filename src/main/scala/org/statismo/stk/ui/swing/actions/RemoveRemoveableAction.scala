package org.statismo.stk.ui.swing.actions

import org.statismo.stk.ui.Removeable
import org.statismo.stk.ui.RemoveableChildren
import org.statismo.stk.ui.SceneTreeObject

class RemoveRemoveableAction extends SceneTreePopupAction("Remove") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    if (context.isDefined && context.get.isInstanceOf[Removeable] && context.get.asInstanceOf[Removeable].isCurrentlyRemoveable) {
      if (context.get.isInstanceOf[RemoveableChildren]) {
        title = "Remove all"
      } else {
        title = "Remove"
      }
      true
    } else {
      false
    }
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val r = context.get.asInstanceOf[Removeable]
      r.remove()
    }
  }
}