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

import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Interactor.Verdict.Block
import scalismo.ui.control.interactor.Recipe
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.model.{ LandmarkNode, SceneNode }

object ReadyForEditing {
  def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
    override def apply()(implicit parent: InteractorType): Delegate[InteractorType] = new ReadyForEditing[InteractorType]()
  }
}

class ReadyForEditing[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](implicit override val parent: ComplexLandmarkingInteractor[InteractorType]) extends ComplexLandmarkingInteractor.Delegate[InteractorType] {
  override def onLandmarkCreationToggled(): Unit = {
    if (parent.isLandmarkCreationEnabled) {
      transitionToReadyForCreating()
    }
  }

  override def mouseMoved(e: MouseEvent): Verdict = {
    def onlyLandmarks(node: SceneNode) = node match {
      case yep: LandmarkNode => true
      case _ => false
    }

    Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, onlyLandmarks)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON1) {
      val pointAndNode = e.viewport.rendererState.pointAndNodeAtPosition(e.getPoint)
      pointAndNode.nodeOption.foreach {
        case lm: LandmarkNode =>
          transitionToEditing(lm)
        case _ =>
      }
    }
    // always prevent clicks from propagating
    Block
  }

  override def mousePressed(e: MouseEvent): Verdict = Recipe.Block2DRotation.mousePressed(e)

  // transitions

  def transitionToReadyForCreating(): Unit = {
    parent.transitionTo(ReadyForCreating.enter)
  }

  def transitionToEditing(lm: LandmarkNode): Unit = {
    parent.transitionTo(Editing.enter(lm))
  }
}
