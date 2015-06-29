package scalismo.ui.swing.actions.scenetree

import java.io.File

import scalismo.ui.{ Saveable, SceneTreeObject }
import scalismo.ui.swing.actions.SaveAction

import scala.util.Try

class SaveSaveableAction extends SceneTreePopupAction("Save to file...") {
  def isContextSupported(context: Option[SceneTreeObject]) = {
    context.isDefined && context.get.isInstanceOf[Saveable] && context.get.asInstanceOf[Saveable].isCurrentlySaveable
  }

  override def apply(context: Option[SceneTreeObject]) = {
    if (isContextSupported(context)) {
      val save = context.get.asInstanceOf[Saveable]
      def doSave(file: File): Try[Unit] = {
        save.saveToFile(file)
      }
      new SaveAction(doSave, save.saveableMetadata).apply()
    }
  }
}