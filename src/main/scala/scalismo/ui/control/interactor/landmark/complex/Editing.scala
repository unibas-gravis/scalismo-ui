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

package scalismo.ui.control.interactor.landmark.complex

import java.awt.Cursor
import java.awt.event.{MouseEvent, MouseWheelEvent}

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{Delegate, StateTransition}
import scalismo.ui.model.{LandmarkNode, StatusMessage}

object Editing {
  def enter[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](lm: LandmarkNode): StateTransition[IT, DT] =
    editAxis(lm, 0)

  def editAxis[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](lm: LandmarkNode,
                                                                           axisIndex: Int): StateTransition[IT, DT] =
    new StateTransition[IT, DT] {
      override def apply()(implicit parent: IT): Delegate[IT] = new Editing[IT](lm, axisIndex)
    }
}

class Editing[IT <: ComplexLandmarkingInteractor[IT]](landmarkNode: LandmarkNode, axisIndex: Int)(
  implicit
  override val parent: ComplexLandmarkingInteractor[IT]
) extends ComplexLandmarkingInteractor.Delegate[IT] {
  override def onLandmarkCreationToggled(): Unit = cancel()

  override def mouseExited(e: MouseEvent): Verdict = {
    cancel()
    super.mouseExited(e)
  }

  val cursorList = List(Cursor.E_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR)

  override def mouseClicked(e: MouseEvent): Verdict = {

    if (e.getButton == MouseEvent.BUTTON2) {
      val nextAxisIndex = (axisIndex + 1) % 3
      e.canvas.setCursor(Cursor.getPredefinedCursor(cursorList(nextAxisIndex)))
      transitionToEditAxis(landmarkNode, nextAxisIndex)
    } else if (e.getButton == MouseEvent.BUTTON3) {
      val cursor =
        if (parent.isLandmarkCreationEnabled) Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
        else Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
      e.canvas.setCursor(cursor)
      cancel()
    }

    Block
  }

  override def mouseWheelMoved(e: MouseWheelEvent): Verdict = {
    val more = {
      if (e.getWheelRotation < 0) true else false
    }
    // this provides a somewhat natural zoom feeling.
    val factor = 1.05f
    val sigmas = landmarkNode.uncertainty.value.sigmas.toArray
    val oldValue = sigmas(axisIndex)
    val newValue = {
      if (more) {
        oldValue * factor
      } else {
        oldValue / factor
      }
    }

    sigmas(axisIndex) = Math.max(0, newValue)
    landmarkNode.uncertainty.value = landmarkNode.uncertainty.value.copy(sigmas = sigmas.toList)
    Block
  }

  def cancel(): Unit = {
    endEditing()
  }

  def endEditing(): Unit = {
    if (parent.isLandmarkCreationEnabled) {
      transitionToReadyForCreating()
    } else {
      transitionToReadyForEditing()
    }
  }

  def transitionToReadyForCreating(): Unit = {
    clearStatus()
    parent.transitionTo(ReadyForCreating.enter)
  }

  def transitionToReadyForEditing(): Unit = {
    clearStatus()
    parent.transitionTo(ReadyForEditing.enter)
  }

  def transitionToEditAxis(node: LandmarkNode, axisIndex: Int): Unit = {
    parent.transitionTo(Editing.editAxis(landmarkNode, axisIndex))
  }

  def showStatus(): Unit = {
    parent.frame.status.set(
      StatusMessage(s"You are editing axis ${axisIndex + 1} of landmark ${landmarkNode.name}",
                    kind = StatusMessage.Information,
                    highPriority = true,
                    log = false)
    )
  }

  def clearStatus(): Unit = {
    parent.frame.status.clear()
  }

  // constructor
  showStatus()
}
