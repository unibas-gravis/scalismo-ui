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

  // transitions

  def transitionToReadyForCreating(): Unit = {
    parent.transitionTo(ReadyForCreating.enter)
  }

  def transitionToEditing(lm: LandmarkNode): Unit = {
    parent.transitionTo(Editing.enter(lm))
  }
}
