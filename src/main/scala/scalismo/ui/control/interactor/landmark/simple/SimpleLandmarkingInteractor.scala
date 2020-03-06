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

package scalismo.ui.control.interactor.landmark.simple

import java.awt.event.MouseEvent
import java.awt.{Color, Cursor}

import javax.swing.SwingUtilities
import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Pass
import scalismo.ui.control.interactor.{DefaultInteractor, Interactor, Recipe}
import scalismo.ui.model.properties.Uncertainty
import scalismo.ui.model.{LandmarkNode, SceneNode}
import scalismo.ui.resources.icons.BundledIcon
import scalismo.ui.view.ScalismoFrame

import scala.swing.ToggleButton
import scala.swing.event.ButtonClicked

trait SimpleLandmarkingInteractorTrait extends Interactor {

  def defaultUncertainty: Uncertainty

  val landmarkingButton: ToggleButton = new ToggleButton {
    private val myIcon = BundledIcon.Landmark

    def updateUi(): Unit = {
      val onOff = if (selected) "ON" else "OFF"
      tooltip = s"Toggle landmarking (currently $onOff)"
      val iconColor = if (selected) Color.GREEN.darker else Color.DARK_GRAY
      icon = myIcon.colored(iconColor).standardSized()
    }

    reactions += {
      case ButtonClicked(_) => updateUi()
    }

    updateUi()
  }

  override def onActivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.add(landmarkingButton)
  }

  override def onDeactivated(frame: ScalismoFrame): Unit = {
    frame.toolbar.remove(landmarkingButton)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {

    if (landmarkingButton.selected && SwingUtilities.isLeftMouseButton(e)) {
      Recipe.AddLandmarkOnClick.mouseClicked(e, defaultUncertainty)
    } else {
      Pass
    }
  }

  // set the cursor to a crosshair if we're in landmarking mode
  override def mouseEntered(e: MouseEvent): Verdict = {
    val cursor =
      if (landmarkingButton.selected) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) else Cursor.getDefaultCursor
    e.canvas.setCursor(cursor)
    super.mouseEntered(e)
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    if (landmarkingButton.selected) {
      def exceptLandmarks(node: SceneNode) = node match {
        case _: LandmarkNode => false
        case _               => true
      }

      Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, exceptLandmarks)
    }
    super.mouseMoved(e)
  }

}

object SimpleLandmarkingInteractor extends SimpleLandmarkingInteractorTrait with DefaultInteractor {

  override val defaultUncertainty: Uncertainty = Uncertainty.DefaultUncertainty

  override def mousePressed(e: MouseEvent): Verdict = Recipe.Block2DRotation.mousePressed(e)
}
