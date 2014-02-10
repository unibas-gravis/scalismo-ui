package org.statismo.stk.ui.swing.actions

import org.statismo.stk.ui.Saveable
import org.statismo.stk.ui.SceneTreeObject
import java.io.File
import scala.util.Try
import org.statismo.stk.ui.Loadable

class LoadAction extends SceneTreePopupAction("Load from file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[Loadable] && context.get.asInstanceOf[Loadable].isCurrentlyLoadable
  }

  def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val load = context.get.asInstanceOf[Loadable]
      def doLoad(file: File): Try[Unit] = {
        load.loadFromFile(file)
      }
      new LoadSceneTreeObjectAction(doLoad, load.loadableMetadata).apply()
    }
  }

}