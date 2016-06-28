package scalismo.ui.view.action.popup

import java.io.File

import scalismo.ui.model.{ GroupNode, LandmarksNode, LandmarkNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.FileIoMetadata
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.SaveAction

import scala.util.Try

object SaveLandmarksAction extends PopupAction.Factory {
  override def apply(nodes: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    val landmarks = allMatch[LandmarkNode](nodes)
    if (landmarks.isEmpty) {
      // could be a landmarkSnode, in which case we should also return save actions
      val landmarksNodeOpt = singleMatch[LandmarksNode](nodes)
      if (landmarksNodeOpt.isEmpty) {
        Nil
      } else {
        if (landmarksNodeOpt.get.children.length > 0) {
          List(new SaveLandmarksAction(landmarksNodeOpt.get.children), new SaveLandmarksAction(landmarksNodeOpt.get.children, false))
        } else Nil
      }
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
class SaveLandmarksAction private (nodes: List[LandmarkNode], transformedFlag: Boolean = true)(implicit val frame: ScalismoFrame) extends PopupAction(s"Save${if (transformedFlag) " transformed" else " original"} ${FileIoMetadata.Landmarks.description} ...", BundledIcon.Save) {
  val landmarks = nodes.head.parent

  def doSave(file: File): Try[Unit] = {
    landmarks.saveNodes(nodes, file, transformedFlag)
  }

  override def apply(): Unit = {
    new SaveAction(doSave, FileIoMetadata.Landmarks, title).apply()
  }
}