/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.view.action.popup

import java.io.File

import scalismo.ui.model.{LandmarkNode, LandmarksNode, SceneNode}
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
        if (landmarksNodeOpt.get.children.nonEmpty) {
          List(new SaveLandmarksAction(landmarksNodeOpt.get.children),
               new SaveLandmarksAction(landmarksNodeOpt.get.children, false))
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
class SaveLandmarksAction private (nodes: List[LandmarkNode], transformedFlag: Boolean = true)(implicit
                                                                                               val frame: ScalismoFrame)
    extends PopupAction(
      s"Save${if (transformedFlag) " transformed" else " original"} ${FileIoMetadata.Landmarks.description} ...",
      BundledIcon.Save
    ) {
  private val landmarks = nodes.head.parent

  def doSave(file: File): Try[Unit] = {
    landmarks.saveNodes(nodes, file, transformedFlag)
  }

  override def apply(): Unit = {
    new SaveAction(doSave, FileIoMetadata.Landmarks, title).apply()
  }
}
