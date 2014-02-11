package org.statismo.stk.ui.swing.actions

import org.statismo.stk.ui.Saveable
import org.statismo.stk.ui.SceneTreeObject
import java.io.File
import scala.util.Try

class ShowInAllViewportsAction extends SceneTreePopupAction("Show in all viewports") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined
  }
  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      context.get.showInAllViewports
    }
  }
}

class HideInAllViewportsAction extends SceneTreePopupAction("Hide in all viewports") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined
  }
  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      context.get.hideInAllViewports
    }
  }
}