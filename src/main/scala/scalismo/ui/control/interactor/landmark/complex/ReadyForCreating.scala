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
import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.Recipe
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.model._

object ReadyForCreating {
  def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
    override def apply()(implicit parent: InteractorType): Delegate[InteractorType] = new ReadyForCreating[InteractorType]()
  }
}

class ReadyForCreating[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit override val parent: ComplexLandmarkingInteractor[InteractorType]) extends ComplexLandmarkingInteractor.Delegate[InteractorType] {
  override def onLandmarkCreationToggled(): Unit = {
    if (!parent.isLandmarkCreationEnabled) {
      transitionToReadyForEditing()
    }
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    def exceptLandmarks(node: SceneNode) = node match {
      case _: LandmarkNode => false
      case _ => true
    }

    Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, exceptLandmarks)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON1) {
      parent.getLandmarkForClick(e) match {
        case Some((lm, group)) =>
          val node = group.landmarks.add(lm.copy(id = group.landmarks.nameGenerator.nextName()))
          e.canvas.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR))
          transitionToEditing(node)
        case _ =>
      }
    }
    Block
  }

  def transitionToReadyForEditing(): Unit = {
    parent.transitionTo(ReadyForEditing.enter)
  }

  def transitionToEditing(lm: LandmarkNode): Unit = {
    parent.transitionTo(Editing.enter(lm))
  }

}

