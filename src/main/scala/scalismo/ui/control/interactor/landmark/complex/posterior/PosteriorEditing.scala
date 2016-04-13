package scalismo.ui.control.interactor.landmark.complex.posterior

import java.awt.event.MouseEvent

import scalismo.ui.control.interactor.Interactor.Verdict
import scalismo.ui.control.interactor.landmark.complex.ComplexLandmarkingInteractor.{ Delegate, StateTransition }
import scalismo.ui.control.interactor.landmark.complex.{ ComplexLandmarkingInteractor, Editing }
import scalismo.ui.model.LandmarkNode

object PosteriorEditing {
  def enter[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](lm: LandmarkNode): StateTransition[IT, DT] = editAxis(lm, 0)

  def editAxis[IT <: ComplexLandmarkingInteractor[IT], DT <: Delegate[IT]](lm: LandmarkNode, axisIndex: Int): StateTransition[IT, DT] = new StateTransition[IT, DT] {
    override def apply()(implicit parent: IT): Delegate[IT] = new PosteriorEditing[IT](lm, axisIndex)
  }
}

class PosteriorEditing[InteractorType <: ComplexLandmarkingInteractor[InteractorType]](landmarkNode: LandmarkNode, axisIndex: Int)(implicit parent: InteractorType) extends Editing[InteractorType](landmarkNode, axisIndex) {

  def interactor: PosteriorLandmarkingInteractor = parent.asInstanceOf[PosteriorLandmarkingInteractor]

  override def cancel(): Unit = {
    if (parent.isLandmarkCreationEnabled) {
      landmarkNode.remove()
    }
    super.cancel()
  }

  override def transitionToEditAxis(node: LandmarkNode, axisIndex: Int): Unit = {
    parent.transitionTo(PosteriorEditing.editAxis(node, axisIndex))
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
      case Some(point) => interactor.updatePreview(landmarkNode, point)
      case None =>
    }
    super.mouseMoved(e)
  }

  // constructor
  interactor.showPreview()

  override def mouseClicked(e: MouseEvent): Verdict = {
    if (e.getButton == MouseEvent.BUTTON1) {
      parent.getLandmarkForClick(e) match {
        case Some((lm, group)) if group == interactor.targetGroupNode =>
          group.landmarks.add(lm.copy(id = landmarkNode.name))
          endEditing()
        case _ =>
      }
    }
    super.mouseClicked(e)
  }
}