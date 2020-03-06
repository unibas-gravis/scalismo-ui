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

package scalismo.ui.control.interactor.landmark.complex.posterior

import java.awt.Cursor
import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{Delegate, StateTransition}
import scalismo.ui.control.interactor.landmark.complex.{ComplexLandmarkingInteractor, Editing}
import scalismo.ui.model.LandmarkNode

object PosteriorEditing {
  def enter[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](
    modelLm: LandmarkNode,
    targetLm: LandmarkNode
  ): StateTransition[IT, DT] = editAxis(modelLm, targetLm, 0)

  def editAxis[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](modelLm: LandmarkNode,
                                                                           targetLm: LandmarkNode,
                                                                           axisIndex: Int): StateTransition[IT, DT] =
    new StateTransition[IT, DT] {
      override def apply()(implicit parent: IT): Delegate[IT] = new PosteriorEditing[IT](modelLm, targetLm, axisIndex)
    }
}

class PosteriorEditing[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](
  modelLm: LandmarkNode,
  targetLm: LandmarkNode,
  axisIndex: Int
)(implicit parent: InteractorType)
    extends Editing[InteractorType](targetLm, axisIndex) {

  def interactor: PosteriorLandmarkingInteractor = parent.asInstanceOf[PosteriorLandmarkingInteractor]

  override def cancel(): Unit = {
    if (parent.isLandmarkCreationEnabled) {
      modelLm.remove()
      targetLm.remove()
    }
    super.cancel()
  }

  override def transitionToEditAxis(targetLm: LandmarkNode, axisIndex: Int): Unit = {
    parent.transitionTo(PosteriorEditing.editAxis(modelLm, targetLm, axisIndex))
  }

  override def transitionToReadyForCreating(): Unit = {
    interactor.hidePreview()
    clearStatus()
    parent.transitionTo(PosteriorReadyForCreating.enter)
  }

  override def transitionToReadyForEditing(): Unit = {
    interactor.hidePreview()
    clearStatus()
    parent.transitionTo(PosteriorReadyForEditing.enter)
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    e.viewport.rendererState.pointAndNodeAtPosition(e.getPoint).pointOption match {
      case Some(point) => interactor.updatePreview(modelLm, targetLm, point)
      case None        =>
    }
    super.mouseMoved(e)
  }

  // constructor
  interactor.showPreview()

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON1) {
      parent.getLandmarkForClick(e) match {
        case Some((lm, group)) if group == interactor.targetGroupNode =>
          group.landmarks.add(
            lm.copy(id = targetLm.name, uncertainty = Some(targetLm.uncertainty.value.toMultivariateNormalDistribution))
          )
          interactor.targetUncertaintyGroup.landmarks.head.remove()
          val cursor =
            if (parent.isLandmarkCreationEnabled) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
            else Cursor.getDefaultCursor
          e.canvas.setCursor(cursor)
          endEditing()
        case _ =>
      }
    }
    super.mouseClicked(e)
  }
}
