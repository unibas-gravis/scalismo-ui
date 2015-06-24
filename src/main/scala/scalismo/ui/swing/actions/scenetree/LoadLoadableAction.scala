package scalismo.ui.swing.actions.scenetree

import java.io.File

import scalismo.ui.swing.actions.LoadAction
import scalismo.ui.{ Loadable, SceneTreeObject }

import scala.util.Try

class LoadLoadableAction extends SceneTreePopupAction("Load from file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[Loadable] && context.get.asInstanceOf[Loadable].isCurrentlyLoadable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val load = context.get.asInstanceOf[Loadable]
      def doLoad(file: File): Try[Unit] = {
        load.loadFromFile(file)
      }
      new LoadAction(doLoad, load.loadableMetadata).apply()
    }
  }
}
