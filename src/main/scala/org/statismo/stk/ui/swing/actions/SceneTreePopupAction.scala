package org.statismo.stk.ui.swing.actions

import scala.swing.MenuItem

import org.statismo.stk.ui.SceneTreeObject

abstract class SceneTreePopupAction(name: String) extends ActionWithContext[SceneTreeObject](name) {
  // you MUST override AT LEAST ONE of these methods. The createMenuItem() one gets invoked first.
  // If (and only if) that returns None, the apply() method is invoked.
  def createMenuItem(context: Option[SceneTreeObject]): Option[MenuItem] = None
  override def apply(context: Option[SceneTreeObject]): Unit = ???
}