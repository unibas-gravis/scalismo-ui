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

import scalismo.io.StatisticalModelIO
import scalismo.ui.model.{ GroupNode, SceneNode }
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.util.{ FileIoMetadata, FileUtil }
import scalismo.ui.view.ScalismoFrame
import scalismo.ui.view.action.LoadAction

import scala.util.Try

object LoadStatisticalVolumeMeshModelAction extends PopupAction.Factory {
  override def apply(context: List[SceneNode])(implicit frame: ScalismoFrame): List[PopupAction] = {
    singleMatch[GroupNode](context).map(n => new LoadStatisticalVolumeMeshModelAction(n)).toList
  }
}

class LoadStatisticalVolumeMeshModelAction(group: GroupNode)(implicit frame: ScalismoFrame)
    extends PopupAction(s"Load ${FileIoMetadata.StatisticalVolumeMeshModel.description} ...", BundledIcon.Load) {

  def load(file: File): Try[Unit] = {

    for {
      model <- StatisticalModelIO.readStatisticalVolumeMeshModel(file)
    } yield {
      val basename = FileUtil.basename(file)
      group.addStatisticalVolumeMeshModel(model, basename)
    }
  }

  override def apply(): Unit = {
    new LoadAction(load, FileIoMetadata.StatisticalVolumeMeshModel).apply()
  }

}