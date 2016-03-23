package scalismo.ui.control.interactor.landmark.complex

import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.Recipe
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.model.{ LandmarkNode, SceneNode }

object ReadyForCreating {
  def enter[InteractorType <: ComplexLandmarkingInteractor[InteractorType], DelegateType <: Delegate[InteractorType]]: StateTransition[InteractorType, DelegateType] = new StateTransition[InteractorType, DelegateType] {
    override def apply()(implicit parent: ComplexLandmarkingInteractor[InteractorType]): Delegate[InteractorType] = new ReadyForCreating[InteractorType]()
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
      case nope: LandmarkNode => false
      case _ => true
    }
    Recipe.HighlightOutlineOfPickableObject.mouseMoved(e, exceptLandmarks)
  }

  override def mouseClicked(e: MouseEvent): Verdict = {
    // FIXME: this is the simple implementation
    Recipe.AddLandmarkOnClick.mouseClicked(e)
  }

  def transitionToReadyForEditing(): Unit = {
    parent.transitionTo(ReadyForEditing.enter)
  }

}

