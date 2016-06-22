package scalismo.ui.view.action.popup

import java.io.File

import scalismo.ui.model.{ LandmarkNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.SaveAction

import scala.util.Try

object SaveLandmarksAction extends PopupAction.Factory {
  override def apply(nodes: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    val landmarks = allMatch[LandmarkNode](nodes)
    if (landmarks.isEmpty) {
      Nil
    } else {
      val groups = landmarks.map(_.group).distinct
      if (groups.length != 1) {
        // landmarks are in different groups, we don't support that
        Nil
      } else {
        List(new SaveLandmarksAction(landmarks), new SaveLandmarksAction(landmarks, false))
      }
    }
  }
}

// the companion object took care of the safety checks, like making sure the list is not empty etc.
class SaveLandmarksAction private (nodes: List[LandmarkNode], transformedFlag: Boolean = true)(implicit val frame: ScalismoFrame) extends PopupAction(s"Save${if (transformedFlag) " transformed" else " original"} ${nodes.head.parent.saveMetadata.description} ...", BundledIcon.Save) {
  val landmarks = nodes.head.parent

  def doSave(file: File): Try[Unit] = {
    landmarks.saveNodes(nodes, file, transformedFlag)
  }

  override def apply(): Unit = {
    new SaveAction(doSave, landmarks.saveMetadata, title).apply()
  }
}